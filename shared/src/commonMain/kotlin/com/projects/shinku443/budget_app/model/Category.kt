@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isActive: Boolean = true
)