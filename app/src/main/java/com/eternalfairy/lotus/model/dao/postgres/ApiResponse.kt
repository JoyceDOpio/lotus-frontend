package com.eternalfairy.lotus.model.dao.postgres

sealed class ApiResponse<out R> {
    data class Success<out R>(val data: R? = null): ApiResponse<R>()
    data class Error(val message: String?): ApiResponse<Nothing>()
//    object Loading: ApiResponse<Nothing>()
}