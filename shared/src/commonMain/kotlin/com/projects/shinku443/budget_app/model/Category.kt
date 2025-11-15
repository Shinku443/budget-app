import com.projects.shinku443.budget_app.model.CategoryType
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isActive: Boolean = true
)