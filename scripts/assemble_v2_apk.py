#!/usr/bin/env python3
import base64
import io
import os
import struct
import zipfile

def get_defined_classes(dex_bytes: bytes) -> list[str]:
    try:
        string_ids_size, string_ids_off, type_ids_size, type_ids_off = struct.unpack_from("<IIII", dex_bytes, 0x38)
        class_defs_size, class_defs_off = struct.unpack_from("<II", dex_bytes, 0x60)
        classes = []
        for i in range(class_defs_size):
            class_idx = struct.unpack_from("<I", dex_bytes, class_defs_off + i * 32)[0]
            descriptor_idx = struct.unpack_from("<I", dex_bytes, type_ids_off + class_idx * 4)[0]
            str_off = struct.unpack_from("<I", dex_bytes, string_ids_off + descriptor_idx * 4)[0]
            pos = str_off
            while dex_bytes[pos] & 0x80:
                pos += 1
            pos += 1
            end = dex_bytes.find(b"\x00", pos)
            classes.append(dex_bytes[pos:end].decode("utf-8", errors="ignore"))
        return classes
    except Exception:
        return []

def main():
    with open("v2_patch.b64", "r") as f:
        b64_lines = [line.strip() for line in f if line.strip() and not line.startswith("#")]
    patch_bytes = base64.b64decode("".join(b64_lines))
    patch_zip = zipfile.ZipFile(io.BytesIO(patch_bytes), "r")
    patch_names = set(patch_zip.namelist())

    patch_dex_names = sorted([fn for fn in patch_zip.namelist() if fn.endswith(".dex")])

    with zipfile.ZipFile("/tmp/libv2ray.aar", "r") as aar:
        libgojni_bytes = aar.read("jni/arm64-v8a/libgojni.so")

    with zipfile.ZipFile("Bush-VPN.apk", "r") as base_apk:
        with zipfile.ZipFile("/tmp/unsigned.apk", "w") as out_apk:
            kept_dex_blobs = []
            base_has_libv2ray = False

            for item in base_apk.infolist():
                fn = item.filename
                if fn.startswith("META-INF/"):
                    continue
                if fn.endswith(".dex"):
                    dex_data = base_apk.read(fn)
                    defined = get_defined_classes(dex_data)
                    is_app_dex = any(
                        c.startswith("Lcom/example/")
                        or c == "Landroidx/compose/material/icons/filled/ExtendedMaterialIconsKt;"
                        for c in defined
                    ) or (len(defined) < 50 and any(c.startswith("Llibv2ray/") for c in defined))

                    if is_app_dex:
                        print(f"Replacing old app DEX from base APK: {fn} ({len(defined)} classes)")
                        continue

                    if any(c.startswith("Llibv2ray/") for c in defined):
                        base_has_libv2ray = True
                    print(f"Keeping library DEX from base APK: {fn} ({len(defined)} classes)")
                    kept_dex_blobs.append(dex_data)
                    continue
                if fn in patch_names or fn == "lib/arm64-v8a/libgojni.so":
                    continue
                info = zipfile.ZipInfo(fn)
                info.compress_type = item.compress_type
                out_apk.writestr(info, base_apk.read(fn))

            # Append all new v2.0 DEX files from v2_patch.b64
            for fn in patch_dex_names:
                if fn == "classes8.dex" and base_has_libv2ray:
                    print("Base library DEX already includes libv2ray; skipping standalone classes8.dex")
                    continue
                dex_data = patch_zip.read(fn)
                kept_dex_blobs.append(dex_data)

            # Write all DEX files with contiguous sequential names: classes.dex, classes2.dex, ...
            for idx, dex_data in enumerate(kept_dex_blobs, start=1):
                dex_name = "classes.dex" if idx == 1 else f"classes{idx}.dex"
                info = zipfile.ZipInfo(dex_name)
                info.compress_type = zipfile.ZIP_DEFLATED
                out_apk.writestr(info, dex_data)

            # Write non-DEX patched entries (AndroidManifest.xml, resources.arsc, res/*)
            for fn in patch_zip.namelist():
                if fn.endswith(".dex"):
                    continue
                info = zipfile.ZipInfo(fn)
                if fn == "resources.arsc":
                    info.compress_type = zipfile.ZIP_STORED
                else:
                    info.compress_type = zipfile.ZIP_DEFLATED
                out_apk.writestr(info, patch_zip.read(fn))

            # Write native Xray-core v26 JNI library
            info = zipfile.ZipInfo("lib/arm64-v8a/libgojni.so")
            info.compress_type = zipfile.ZIP_DEFLATED
            out_apk.writestr(info, libgojni_bytes)

    print(f"Assembled /tmp/unsigned.apk ({os.path.getsize('/tmp/unsigned.apk')} bytes, {len(kept_dex_blobs)} DEX files)")

if __name__ == "__main__":
    main()
