package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.repository.UserRepository
import javax.inject.Inject

class InitializeDeviceUuidUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<String> = userRepository.getOrCreateDeviceId()
}
