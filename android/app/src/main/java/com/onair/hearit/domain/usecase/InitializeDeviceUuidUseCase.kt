package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.repository.UserRepository

class InitializeDeviceUuidUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<String> = userRepository.getOrCreateUserId()
}
