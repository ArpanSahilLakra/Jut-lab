import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.runBlocking
import com.example.network.SupabaseClient

fun main() = runBlocking {
    SupabaseClient.client.auth.awaitInitialization()
}
