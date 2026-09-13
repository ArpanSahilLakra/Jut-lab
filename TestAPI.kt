import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.status.RefreshFailureCause

fun test(status: SessionStatus) {
    if (status is SessionStatus.RefreshFailure) {
        val cause = status.cause
        if (cause is RefreshFailureCause.NetworkError) {
            
        }
    }
}
