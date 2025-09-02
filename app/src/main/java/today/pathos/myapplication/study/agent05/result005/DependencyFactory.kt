package today.pathos.myapplication.study.agent05.result005

// Factory interfaces
interface DataSourceFactory {
    fun createLocalDataSource(): ItemDataSource
    fun createRemoteDataSource(): ItemDataSource
}

interface RepositoryFactory {
    fun createItemRepository(
        localDataSource: ItemDataSource,
        remoteDataSource: ItemDataSource
    ): ItemRepository
}

interface ServiceFactory {
    fun createItemService(repository: ItemRepository): ItemService
}

interface ViewModelFactory {
    fun createViewModel(service: ItemService): Agent05ViewModel
}

// Concrete factory implementations
class DefaultDataSourceFactory : DataSourceFactory {
    override fun createLocalDataSource(): ItemDataSource = LocalItemDataSource()
    override fun createRemoteDataSource(): ItemDataSource = RemoteItemDataSource()
}

class DefaultRepositoryFactory : RepositoryFactory {
    override fun createItemRepository(
        localDataSource: ItemDataSource,
        remoteDataSource: ItemDataSource
    ): ItemRepository = ItemRepositoryImpl(localDataSource, remoteDataSource)
}

class DefaultServiceFactory : ServiceFactory {
    override fun createItemService(repository: ItemRepository): ItemService = 
        ItemServiceImpl(repository)
}

class DefaultViewModelFactory : ViewModelFactory {
    override fun createViewModel(service: ItemService): Agent05ViewModel = 
        Agent05ViewModel(service)
}

// Dependency Injection Container
class DIContainer(
    private val dataSourceFactory: DataSourceFactory = DefaultDataSourceFactory(),
    private val repositoryFactory: RepositoryFactory = DefaultRepositoryFactory(),
    private val serviceFactory: ServiceFactory = DefaultServiceFactory(),
    private val viewModelFactory: ViewModelFactory = DefaultViewModelFactory()
) {
    
    // Lazy initialization of dependencies
    private val localDataSource: ItemDataSource by lazy {
        dataSourceFactory.createLocalDataSource()
    }
    
    private val remoteDataSource: ItemDataSource by lazy {
        dataSourceFactory.createRemoteDataSource()
    }
    
    private val repository: ItemRepository by lazy {
        repositoryFactory.createItemRepository(localDataSource, remoteDataSource)
    }
    
    private val service: ItemService by lazy {
        serviceFactory.createItemService(repository)
    }
    
    fun provideViewModel(): Agent05ViewModel {
        return viewModelFactory.createViewModel(service)
    }
    
    // For testing or configuration changes
    fun provideService(): ItemService = service
    fun provideRepository(): ItemRepository = repository
    fun provideLocalDataSource(): ItemDataSource = localDataSource
    fun provideRemoteDataSource(): ItemDataSource = remoteDataSource
}

// Abstract Factory for different configurations
abstract class AbstractDependencyFactory {
    abstract fun createDataSourceFactory(): DataSourceFactory
    abstract fun createRepositoryFactory(): RepositoryFactory
    abstract fun createServiceFactory(): ServiceFactory
    abstract fun createViewModelFactory(): ViewModelFactory
    
    fun createDIContainer(): DIContainer {
        return DIContainer(
            dataSourceFactory = createDataSourceFactory(),
            repositoryFactory = createRepositoryFactory(),
            serviceFactory = createServiceFactory(),
            viewModelFactory = createViewModelFactory()
        )
    }
}

// Production factory
class ProductionDependencyFactory : AbstractDependencyFactory() {
    override fun createDataSourceFactory(): DataSourceFactory = DefaultDataSourceFactory()
    override fun createRepositoryFactory(): RepositoryFactory = DefaultRepositoryFactory()
    override fun createServiceFactory(): ServiceFactory = DefaultServiceFactory()
    override fun createViewModelFactory(): ViewModelFactory = DefaultViewModelFactory()
}

// Test factory (for future testing scenarios)
class TestDependencyFactory : AbstractDependencyFactory() {
    override fun createDataSourceFactory(): DataSourceFactory = DefaultDataSourceFactory() // Could be test doubles
    override fun createRepositoryFactory(): RepositoryFactory = DefaultRepositoryFactory()
    override fun createServiceFactory(): ServiceFactory = DefaultServiceFactory()
    override fun createViewModelFactory(): ViewModelFactory = DefaultViewModelFactory()
}

// Singleton DI provider
object DependencyProvider {
    private var container: DIContainer? = null
    
    fun initialize(factory: AbstractDependencyFactory = ProductionDependencyFactory()) {
        container = factory.createDIContainer()
    }
    
    fun getContainer(): DIContainer {
        return container ?: run {
            initialize()
            container!!
        }
    }
    
    fun reset() {
        container = null
    }
}