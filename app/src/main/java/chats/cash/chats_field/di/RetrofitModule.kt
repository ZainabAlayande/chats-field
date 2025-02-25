package chats.cash.chats_field.di

import androidx.core.content.ContextCompat
import chats.cash.chats_field.R
import chats.cash.chats_field.network.datasource.RetrofitDataSource
import chats.cash.chats_field.network.repository.BeneficiaryRepository
import chats.cash.chats_field.utils.ChatsFieldConstants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

val retrofitModule = module {

    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .disableHtmlEscaping().create()
    }

    fun provideHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClientBuilder
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .protocols( Collections.singletonList(Protocol.HTTP_1_1) )
        return okHttpClientBuilder.build()
    }

    fun provideRetrofit(factory: Gson, client: OkHttpClient, callAdapterFactory: CoroutineCallAdapterFactory): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ChatsFieldConstants.BASE_URL)
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(GsonConverterFactory.create(factory))
            .client(client)
            .build()
    }

    single { provideGson() }
    single { provideHttpClient(get()) }
    single { HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY } }
    single { CoroutineCallAdapterFactory() }
    single { provideRetrofit(get(), get(), get()) }

    single { RetrofitDataSource(get(),androidContext().getString(R.string.an_unknown_error_occured))}
    single{ BeneficiaryRepository(get(), get(), get()) }
}
