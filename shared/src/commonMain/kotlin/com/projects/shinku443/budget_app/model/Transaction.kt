import com.projects.shinku443.budget_app.model.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val date: String, // ISO 8601
    val amount: Double,
    val categoryId: String,
    val type: TransactionType,
    val notes: String? = null
)