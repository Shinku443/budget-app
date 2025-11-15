import com.projects.shinku443.budget_app.model.CategoryType
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val type: CategoryType,
    val categoryId: String,   // ✅ store just the ID
    val date: String
)
