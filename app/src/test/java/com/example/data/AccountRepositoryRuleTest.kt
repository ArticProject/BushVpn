package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserAccount_and_createVerificationCode_succeedsForOwner() = runBlocking {
        val uid = signInTestUser("alice@bushvpn.io")
        val repo = AccountRepository(firestore)

        val saveRes = withTimeout(5000L) {
            repo.saveUserAccount(
                email = "alice@bushvpn.io",
                name = "Alice",
                username = "alice_vpn",
                emailVerified = false
            )
        }
        assertTrue(saveRes.isSuccess)
        assertEquals(uid, saveRes.getOrThrow())

        val codeRes = withTimeout(5000L) {
            repo.createVerificationCode("alice@bushvpn.io", "482915")
        }
        assertTrue(codeRes.isSuccess)
        val codeId = codeRes.getOrThrow()

        val verifyRes = withTimeout(5000L) {
            repo.markCodeVerified(codeId)
        }
        assertTrue(verifyRes.isSuccess)

        val emitted = withTimeout(3000L) {
            repo.observeVerificationCodes().first { list -> list.any { it.id == codeId && it.verified } }
        }
        assertTrue(emitted.any { it.code == "482915" && it.verified })
    }

    @Test
    fun getUserAccount_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice2@bushvpn.io")
        val aliceRepo = AccountRepository(firestore)
        withTimeout(5000L) {
            aliceRepo.saveUserAccount(
                email = "alice2@bushvpn.io",
                name = "Alice",
                username = "alice_vpn",
                emailVerified = true
            ).getOrThrow()
        }

        signInTestUser("bob2@bushvpn.io")
        val bobRepo = AccountRepository(firestore)
        val bobReadAlice = withTimeout(5000L) {
            bobRepo.getUserAccount(targetUserId = aliceUid)
        }
        assertTrue(bobReadAlice.isFailure)
        val fsEx = bobReadAlice.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeVerificationCodes_unauthenticated_fails() = runBlocking {
        auth.signOut()
        val repo = AccountRepository(firestore)
        var caught: Throwable? = null
        try {
            withTimeout(3000L) {
                repo.observeVerificationCodes().first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull(caught)
    }
}
