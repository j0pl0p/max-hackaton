package org.white_powerbank.app.di

import org.white_powerbank.bot.MaxBotService
import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.repositories.*
import org.white_powerbank.repositories.*
import org.white_powerbank.usecases.*
import org.koin.dsl.module

val appModule = module {
    // Data layer repositories
    single<UsersRepository> { UsersRepositoryImpl() }
    single<NotesRepository> { NotesRepositoryImpl() }
    single<AwardsRepository> { AwardsRepositoryImpl() }
    single<UsersAwardsRepository> { UsersAwardsRepositoryImpl() }
    
    // Domain layer use cases
    single { MatchPartnersUseCase(get()) }
    single { SearchPairUseCase(get(), get()) }
    single { ChangePartnerUseCase(get(), get()) }
    single { GetPartnerInfoUseCase(get(), get()) }
    single { GetStatisticsUseCase(get(), get()) }
    single { GetAchievementsUseCase(get(), get()) }
    single { StartQuitUseCase(get()) }
    single { SaveNoteUseCase(get()) }
    single { RestartQuitUseCase(get()) }
    single { NoteUseCase(get(), get()) }
    single { StopSearchUseCase(get()) }
    single { RelapseUseCase(get()) }
    
    // Bot layer
    single { UserStateManager(get()) }
    single { MaxBotService(getProperty("bot.token"), get(), get(), get(), get(), get()) }
}