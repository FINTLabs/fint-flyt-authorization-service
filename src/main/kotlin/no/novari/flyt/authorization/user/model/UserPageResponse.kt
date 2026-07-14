package no.novari.flyt.authorization.user.model

data class UserPageResponse(
    val content: List<User>,
    val totalPages: Int,
)
