with open('app/src/main/java/com/example/repository/AuthRepository.kt', 'r') as f:
    code = f.read()

# Replace the incorrect signInWith call
old_call = """    suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            auth.signInWith(io.github.jan.supabase.auth.providers.builtin.OAuth, io.github.jan.supabase.auth.providers.Google) {
                // scheme and host matching manifest if needed, or default
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""

new_call = """    suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            auth.signInWith(io.github.jan.supabase.auth.providers.Google)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }"""

code = code.replace(old_call, new_call)

with open('app/src/main/java/com/example/repository/AuthRepository.kt', 'w') as f:
    f.write(code)
