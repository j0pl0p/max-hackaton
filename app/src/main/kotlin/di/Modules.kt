package org.white_powerbank.app.di

import org.koin.dsl.module
import org.white_powerbank.bot.MaxBotService
import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.bot.handlers.*
import org.white_powerbank.repositories.*
import org.white_powerbank.usecases.*

val appModule = module {
    // Data layer - Repositories
    single<UsersRepository> { UsersRepositoryImpl() }
    single<NotesRepository> { NotesRepositoryImpl() }
    single<AwardsRepository> { AwardsRepositoryImpl() }
    single<UsersAwardsRepository> { UsersAwardsRepositoryImpl() }
    
    // Domain layer - Use Cases
    single { MatchPartnersUseCase(get()) }
    single { SearchPairUseCase(get(), get()) }
    single { ChangePartnerUseCase(get(), get()) }
    single { GetPartnerInfoUseCase(get(), get()) }
    single { GetStatisticsUseCase(get(), get()) }
    single { GetAchievementsUseCase(get(), get()) }
    single { StartQuitUseCase(get()) }
    single { SaveNoteUseCase(get()) }
    single { RestartQuitUseCase(get()) }
    single { DiaryEntryDataUseCase(get(), get()) }
    
    // Bot layer - State Management
    single { UserStateManager(get()) }
    
    // Bot layer - Handlers
    single { MainMenuHandler(get(), get()) }
    single { PartnerHandler(get(), get(), get(), get(), get()) }
    single { StatisticsHandler(get(), get(), get()) }
    single { DiaryHandler(get(), get(), get(), get()) }
    single { RelapseHandler(get(), get()) }
    
    // Bot layer - Service
    single { MaxBotService(getProperty("bot.token"), get(), get(), get(), get()) }
}