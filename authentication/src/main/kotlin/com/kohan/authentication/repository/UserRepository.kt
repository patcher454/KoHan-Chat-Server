package com.kohan.authentication.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import com.kohan.shared.collection.user.UserCollection

interface UserRepository : MongoRepository<UserCollection, ObjectId> {
    fun findByEmail(email: String): UserCollection?

    fun existsByEmail(email: String): Boolean

    @Query("{'tokenInfos.token': ?0}")
    fun findByTokenInfosToken(token: String): UserCollection?
}
