# Java Spring Boot Developer Roadmap - Part 2

## Advanced Topics (Sections 6-9)

---

## 6. Advanced Features

### Spring Actuator

**Production-Ready Features and Monitoring:**

```java
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;

// application.yml configuration
/*
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,beans,mappings
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
  info:
    env:
      enabled: true
    java:
      enabled: true
    os:
      enabled: true

info:
  app:
    name: @project.name@
    version: @project.version@
    description: My Spring Boot Application
*/

// Custom Health Indicator
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Autowired
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Available")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}

// Reactive Health Indicator
@Component
public class CustomReactiveHealthIndicator implements ReactiveHealthIndicator {
    
    private final ExternalServiceClient externalService;
    
    @Override
    public Mono<Health> health() {
        return externalService.ping()
            .map(response -> Health.up()
                .withDetail("service", "External API")
                .withDetail("latency", response.getLatency())
                .build())
            .onErrorResume(ex -> Mono.just(Health.down(ex)
                .build()));
    }
}

// Custom Info Contributor
@Component
public class CustomInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("custom", Map.of(
            "key1", "value1",
            "key2", "value2",
            "buildTime", LocalDateTime.now()
        ));
    }
}

// Custom Endpoint
@Component
@Endpoint(id = "custom")
public class CustomEndpoint {
    
    @ReadOperation
    public Map<String, Object> customInfo() {
        return Map.of(
            "status", "active",
            "uptime", getUptime(),
            "activeUsers", getActiveUsers()
        );
    }
    
    @WriteOperation
    public void updateConfig(@Selector String key, String value) {
        // Update configuration
    }
    
    @DeleteOperation
    public void resetConfig(@Selector String key) {
        // Reset configuration
    }
    
    private long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
    
    private int getActiveUsers() {
        // Return active user count
        return 42;
    }
}

// Metrics with Micrometer
@Service
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    private final Gauge activeOrdersGauge;
    
    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.orderCounter = Counter.builder("orders.created")
            .description("Total number of orders created")
            .tags("type", "online")
            .register(meterRegistry);
        
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("Order processing time")
            .register(meterRegistry);
        
        // Gauge for active orders
        AtomicInteger activeOrders = new AtomicInteger(0);
        this.activeOrdersGauge = Gauge.builder("orders.active", 
                                              activeOrders, 
                                              AtomicInteger::get)
            .description("Number of active orders")
            .register(meterRegistry);
    }
    
    public void recordOrder() {
        orderCounter.increment();
    }
    
    public void processOrder() {
        orderProcessingTimer.record(() -> {
            // Order processing logic
        });
    }
    
    // Custom metrics with tags
    public void recordPayment(String paymentType, double amount) {
        meterRegistry.counter("payments.processed", 
            "type", paymentType)
            .increment();
        
        meterRegistry.summary("payments.amount",
            "type", paymentType)
            .record(amount);
    }
    
    // Distribution summary for percentiles
    public void recordResponseTime(long milliseconds) {
        DistributionSummary.builder("api.response.time")
            .baseUnit("milliseconds")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry)
            .record(milliseconds);
    }
}

// Prometheus integration
@Configuration
public class PrometheusConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "application", "my-app",
                "environment", "production"
            );
    }
}
```

### Caching

**Caching Strategies:**

```java
import org.springframework.cache.annotation.*;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import java.time.Duration;

// Cache configuration
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("users",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("products",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(15)))
            .build();
    }
    
    // Multiple cache managers
    @Bean("memoryCacheManager")
    public CacheManager memoryCacheManager() {
        return new ConcurrentMapCacheManager("shortLived");
    }
}

// Using cache annotations
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    
    // Cache the result
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        System.out.println("Fetching from database: " + id);
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }
    
    // Cache with condition
    @Cacheable(value = "products", key = "#id", 
               condition = "#id > 0",
               unless = "#result == null")
    public Product getProductConditional(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    // Cache multiple return values
    @Cacheable(value = "products", key = "#category")
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    // Update cache
    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }
    
    // Evict cache entry
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    // Evict all entries
    @CacheEvict(value = "products", allEntries = true)
    public void clearAllProducts() {
        // Clear cache
    }
    
    // Multiple cache operations
    @Caching(
        cacheable = {
            @Cacheable(value = "products", key = "#id")
        },
        put = {
            @CachePut(value = "productDetails", key = "#id")
        },
        evict = {
            @CacheEvict(value = "allProducts", allEntries = true)
        }
    )
    public Product complexCacheOperation(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    // Cache with specific cache manager
    @Cacheable(value = "shortLived", 
               cacheManager = "memoryCacheManager",
               key = "#id")
    public String getTemporaryData(String id) {
        return "Data for " + id;
    }
    
    // Custom key generator
    @Cacheable(value = "customKey", 
               keyGenerator = "customKeyGenerator")
    public Product getProductCustomKey(String name, String category) {
        return productRepository.findByNameAndCategory(name, category);
    }
}

// Custom key generator
@Component("customKeyGenerator")
public class CustomCacheKeyGenerator implements KeyGenerator {
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return target.getClass().getSimpleName() + "_" +
               method.getName() + "_" +
               Arrays.stream(params)
                   .map(String::valueOf)
                   .collect(Collectors.joining("_"));
    }
}

// Programmatic cache access
@Service
public class CacheManagementService {
    
    private final CacheManager cacheManager;
    
    @Autowired
    public CacheManagementService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    public void manualCacheOperations() {
        Cache cache = cacheManager.getCache("products");
        
        // Put value
        cache.put("key1", "value1");
        
        // Get value
        Cache.ValueWrapper wrapper = cache.get("key1");
        if (wrapper != null) {
            String value = (String) wrapper.get();
        }
        
        // Evict
        cache.evict("key1");
        
        // Clear all
        cache.clear();
    }
    
    // Warm up cache
    @PostConstruct
    public void warmUpCache() {
        List<Product> popularProducts = productRepository
            .findTop100ByOrderByViewsDesc();
        
        Cache cache = cacheManager.getCache("products");
        popularProducts.forEach(product -> 
            cache.put(product.getId(), product));
    }
}

// Cache statistics and monitoring
@Component
public class CacheMonitor {
    
    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000)
    public void monitorCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof RedisCache) {
                // Monitor Redis cache statistics
                recordCacheMetrics(cacheName);
            }
        });
    }
    
    private void recordCacheMetrics(String cacheName) {
        // Record custom metrics
        meterRegistry.gauge("cache.size", 
            Tags.of("cache", cacheName), 
            getCacheSize(cacheName));
    }
    
    private long getCacheSize(String cacheName) {
        // Get cache size
        return 0;
    }
}
```

### WebFlux (Reactive Programming)

**Building Reactive Applications:**

```java
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.*;
import org.springframework.http.MediaType;

// Reactive Repository
public interface ReactiveProductRepository 
        extends ReactiveCrudRepository<Product, Long> {
    
    Flux<Product> findByCategory(String category);
    Flux<Product> findByPriceBetween(Double min, Double max);
    Mono<Product> findByName(String name);
}

// Reactive Service
@Service
public class ReactiveProductService {
    
    private final ReactiveProductRepository repository;
    
    @Autowired
    public ReactiveProductService(ReactiveProductRepository repository) {
        this.repository = repository;
    }
    
    public Mono<Product> getProduct(Long id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(
                new ProductNotFoundException("Product not found: " + id)));
    }
    
    public Flux<Product> getAllProducts() {
        return repository.findAll();
    }
    
    public Mono<Product> createProduct(Product product) {
        return repository.save(product);
    }
    
    public Mono<Product> updateProduct(Long id, Product product) {
        return repository.findById(id)
            .flatMap(existing -> {
                existing.setName(product.getName());
                existing.setPrice(product.getPrice());
                return repository.save(existing);
            })
            .switchIfEmpty(Mono.error(
                new ProductNotFoundException("Product not found: " + id)));
    }
    
    public Mono<Void> deleteProduct(Long id) {
        return repository.deleteById(id);
    }
    
    // Complex reactive operations
    public Flux<Product> getProductsWithExternalPricing() {
        return repository.findAll()
            .flatMap(product -> 
                externalPricingService.getPrice(product.getId())
                    .map(price -> {
                        product.setPrice(price);
                        return product;
                    })
            );
    }
    
    // Parallel processing
    public Flux<Product> processProductsInParallel() {
        return repository.findAll()
            .parallel()
            .runOn(Schedulers.parallel())
            .map(this::enrichProduct)
            .sequential();
    }
    
    // Error handling
    public Mono<Product> getProductWithFallback(Long id) {
        return repository.findById(id)
            .onErrorResume(ex -> {
                log.error("Error fetching product", ex);
                return Mono.just(getDefaultProduct());
            })
            .retry(3);
    }
}

// Reactive Controller
@RestController
@RequestMapping("/api/reactive/products")
public class ReactiveProductController {
    
    private final ReactiveProductService productService;
    
    @GetMapping("/{id}")
    public Mono<Product> getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }
    
    @GetMapping
    public Flux<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    // Server-Sent Events
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamProducts() {
        return productService.getAllProducts()
            .delayElements(Duration.ofSeconds(1));
    }
    
    @PostMapping
    public Mono<Product> createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }
    
    @PutMapping("/{id}")
    public Mono<Product> updateProduct(@PathVariable Long id, 
                                       @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }
    
    @DeleteMapping("/{id}")
    public Mono<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}

// Functional routing
@Configuration
public class RouterConfig {
    
    @Bean
    public RouterFunction<ServerResponse> productRoutes(
            ProductHandler handler) {
        return RouterFunctions
            .route(GET("/api/products/{id}"), handler::getProduct)
            .andRoute(GET("/api/products"), handler::getAllProducts)
            .andRoute(POST("/api/products"), handler::createProduct)
            .andRoute(PUT("/api/products/{id}"), handler::updateProduct)
            .andRoute(DELETE("/api/products/{id}"), handler::deleteProduct);
    }
}

@Component
public class ProductHandler {
    
    private final ReactiveProductService productService;
    
    public Mono<ServerResponse> getProduct(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return productService.getProduct(id)
            .flatMap(product -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(productService.getAllProducts(), Product.class);
    }
    
    public Mono<ServerResponse> createProduct(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        return productMono
            .flatMap(productService::createProduct)
            .flatMap(product -> ServerResponse
                .status(HttpStatus.CREATED)
                .bodyValue(product));
    }
}

// WebClient for reactive HTTP calls
@Service
public class ReactiveExternalService {
    
    private final WebClient webClient;
    
    @Autowired
    public ReactiveExternalService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://api.example.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, 
                          MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    public Mono<ExternalData> fetchData(String id) {
        return webClient.get()
            .uri("/data/{id}", id)
            .retrieve()
            .bodyToMono(ExternalData.class)
            .timeout(Duration.ofSeconds(5))
            .retry(3);
    }
    
    public Flux<ExternalData> fetchAllData() {
        return webClient.get()
            .uri("/data")
            .retrieve()
            .bodyToFlux(ExternalData.class);
    }
    
    public Mono<ExternalData> postData(ExternalData data) {
        return webClient.post()
            .uri("/data")
            .bodyValue(data)
            .retrieve()
            .bodyToMono(ExternalData.class);
    }
    
    // Error handling
    public Mono<ExternalData> fetchWithErrorHandling(String id) {
        return webClient.get()
            .uri("/data/{id}", id)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, 
                response -> Mono.error(new ClientException()))
            .onStatus(HttpStatus::is5xxServerError,
                response -> Mono.error(new ServerException()))
            .bodyToMono(ExternalData.class);
    }
}

// Backpressure handling
@Service
public class BackpressureService {
    
    public Flux<Integer> generateNumbers() {
        return Flux.range(1, 100)
            .onBackpressureBuffer(10)  // Buffer up to 10 items
            .delayElements(Duration.ofMillis(10));
    }
    
    public Flux<Integer> handleBackpressure() {
        return Flux.range(1, 1000)
            .onBackpressureDrop()  // Drop items if consumer can't keep up
            .onBackpressureLatest()  // Keep only latest item
            .onBackpressureError();  // Throw error on backpressure
    }
}
```

---

## 7. Microservices & Spring Cloud

### API Gateway

**Building API Gateway with Spring Cloud Gateway:**

```java
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.filter.*;

// Configuration-based routing
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Simple routing
            .route("user-service", r -> r
                .path("/api/users/**")
                .uri("lb://user-service"))
            
            // With predicates
            .route("product-service", r -> r
                .path("/api/products/**")
                .and()
                .method(HttpMethod.GET)
                .and()
                .header("X-Request-Type", "read")
                .uri("lb://product-service"))
            
            // With filters
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .addRequestHeader("X-Gateway", "Spring-Cloud-Gateway")
                    .addResponseHeader("X-Response-Time", 
                        String.valueOf(System.currentTimeMillis()))
                    .rewritePath("/api/orders/(?<segment>.*)", 
                                "/${segment}")
                    .circuitBreaker(config -> config
                        .setName("orderServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallback/orders"))
                )
                .uri("lb://order-service"))
            
            // Rate limiting
            .route("limited-service", r -> r
                .path("/api/limited/**")
                .filters(f -> f
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://limited-service"))
            
            // Retry
            .route("retry-service", r -> r
                .path("/api/retry/**")
                .filters(f -> f
                    .retry(config -> config
                        .setRetries(3)
                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                        .setMethods(HttpMethod.GET)
                        .setBackoff(Duration.ofMillis(100), 
                                   Duration.ofMillis(1000), 
                                   2, true))
                )
                .uri("lb://retry-service"))
            
            .build();
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // replenishRate, burstCapacity
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id")
        );
    }
}

// Custom Global Filter
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(
        LoggingGlobalFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                log.info("Response: {}", response.getStatusCode());
            }));
    }
    
    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}

// Custom Gateway Filter Factory
@Component
public class AuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthenticationGatewayFilterFactory(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (!request.getHeaders().containsKey("Authorization")) {
                throw new UnauthorizedException("Missing authorization header");
            }
            
            String token = request.getHeaders()
                .getFirst("Authorization")
                .substring(7);
            
            if (!jwtTokenProvider.validateToken(token)) {
                throw new UnauthorizedException("Invalid token");
            }
            
            // Add user info to headers
            String username = jwtTokenProvider.getUsername(token);
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Name", username)
                .build();
            
            return chain.filter(exchange.mutate()
                .request(modifiedRequest)
                .build());
        };
    }
    
    public static class Config {
        // Configuration properties
    }
}

// YAML configuration alternative
/*
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Gateway, Spring-Cloud-Gateway
        
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
            - Method=GET
          filters:
            - name: CircuitBreaker
              args:
                name: productServiceCircuitBreaker
                fallbackUri: forward:/fallback/products
        
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
*/

// Fallback controller
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/products")
    public Mono<ResponseEntity<String>> productFallback() {
        return Mono.just(ResponseEntity.ok(
            "Product service is temporarily unavailable"));
    }
    
    @GetMapping("/orders")
    public Mono<ResponseEntity<String>> orderFallback() {
        return Mono.just(ResponseEntity.ok(
            "Order service is temporarily unavailable"));
    }
}
```

### Service Discovery (Eureka)

**Service Registration and Discovery:**

```java
// Eureka Server
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

// application.yml for Eureka Server
/*
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
*/

// Eureka Client (Microservice)
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

// application.yml for Eureka Client
/*
spring:
  application:
    name: user-service

server:
  port: 8081

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    fetchRegistry: true
    registerWithEureka: true
  instance:
    preferIpAddress: true
    instanceId: ${spring.application.name}:${random.value}
    leaseRenewalIntervalInSeconds: 10
    leaseExpirationDurationInSeconds: 30
    metadata-map:
      version: 1.0.0
      region: us-east-1
*/

// Using DiscoveryClient
@Service
public class ServiceDiscoveryService {
    
    private final DiscoveryClient discoveryClient;
    
    @Autowired
    public ServiceDiscoveryService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    
    public List<String> getServiceInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName)
            .stream()
            .map(instance -> instance.getUri().toString())
            .collect(Collectors.toList());
    }
    
    public List<String> getAllServices() {
        return discoveryClient.getServices();
    }
}

// Load-balanced RestTemplate
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Service
public class InterServiceCommunication {
    
    private final RestTemplate restTemplate;
    
    @Autowired
    public InterServiceCommunication(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public User getUserFromUserService(Long userId) {
        // Service name instead of hardcoded URL
        String url = "http://user-service/api/users/" + userId;
        return restTemplate.getForObject(url, User.class);
    }
    
    public Order createOrder(Order order) {
        String url = "http://order-service/api/orders";
        return restTemplate.postForObject(url, order, Order.class);
    }
}

// Feign Client for declarative REST calls
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);
    
    @GetMapping("/api/users")
    List<User> getAllUsers();
    
    @PostMapping("/api/users")
    User createUser(@RequestBody User user);
    
    @PutMapping("/api/users/{id}")
    User updateUser(@PathVariable("id") Long id, @RequestBody User user);
    
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}

// Feign fallback
@Component
public class UserServiceFallback implements UserServiceClient {
    
    @Override
    public User getUserById(Long id) {
        return new User(); // Return default user
    }
    
    @Override
    public List<User> getAllUsers() {
        return Collections.emptyList();
    }
    
    @Override
    public User createUser(User user) {
        throw new ServiceUnavailableException("User service unavailable");
    }
    
    @Override
    public User updateUser(Long id, User user) {
        throw new ServiceUnavailableException("User service unavailable");
    }
    
    @Override
    public void deleteUser(Long id) {
        throw new ServiceUnavailableException("User service unavailable");
    }
}

// Using Feign Client
@Service
public class OrderService {
    
    private final UserServiceClient userServiceClient;
    private final OrderRepository orderRepository;
    
    @Autowired
    public OrderService(UserServiceClient userServiceClient,
                       OrderRepository orderRepository) {
        this.userServiceClient = userServiceClient;
        this.orderRepository = orderRepository;
    }
    
    public Order createOrder(OrderRequest request) {
        // Call user service to verify user
        User user = userServiceClient.getUserById(request.getUserId());
        
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        
        Order order = new Order(request, user);
        return orderRepository.save(order);
    }
}
```

### Resilience Patterns

**Circuit Breaker, Retry, Bulkhead:**

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

// application.yml configuration
/*
resilience4j:
  circuitbreaker:
    instances:
      userService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
        recordExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignoreExceptions:
          - com.example.BusinessException
  
  retry:
    instances:
      userService:
        maxAttempts: 3
        waitDuration: 1s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.io.IOException
        ignoreExceptions:
          - com.example.BusinessException
  
  bulkhead:
    instances:
      userService:
        maxConcurrentCalls: 10
        maxWaitDuration: 1s
  
  ratelimiter:
    instances:
      userService:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0s
  
  timelimiter:
    instances:
      userService:
        timeoutDuration: 2s
        cancelRunningFuture: true
*/

@Service
public class ResilientUserService {
    
    private final UserServiceClient userServiceClient;
    private final RestTemplate restTemplate;
    
    // Circuit Breaker
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(Long id) {
        return userServiceClient.getUserById(id);
    }
    
    private User getUserFallback(Long id, Exception ex) {
        log.error("Fallback method called for user: {}", id, ex);
        return new User(id, "Fallback User", "fallback@example.com");
    }
    
    // Retry
    @Retry(name = "userService", fallbackMethod = "getUserFallback")
    public User getUserWithRetry(Long id) {
        return userServiceClient.getUserById(id);
    }
    
    // Bulkhead
    @Bulkhead(name = "userService", fallbackMethod = "getUserFallback")
    public User getUserWithBulkhead(Long id) {
        return userServiceClient.getUserById(id);
    }
    
    // Rate Limiter
    @RateLimiter(name = "userService", fallbackMethod = "rateLimitFallback")
    public User getUserWithRateLimit(Long id) {
        return userServiceClient.getUserById(id);
    }
    
    private User rateLimitFallback(Long id, Exception ex) {
        log.warn("Rate limit exceeded for user: {}", id);
        throw new RateLimitExceededException("Too many requests");
    }
    
    // Time Limiter (for async operations)
    @TimeLimiter(name = "userService", fallbackMethod = "getUserFallback")
    public CompletableFuture<User> getUserAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> 
            userServiceClient.getUserById(id));
    }
    
    // Combining multiple patterns
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    @Bulkhead(name = "userService")
    @RateLimiter(name = "userService")
    public User getUserWithAllPatterns(Long id) {
        return userServiceClient.getUserById(id);
    }
}

// Programmatic usage
@Service
public class ProgrammaticResilientService {
    
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker;
    private final io.github.resilience4j.retry.Retry retry;
    
    @Autowired
    public ProgrammaticResilientService(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {
        this.circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("userService");
        this.retry = retryRegistry.retry("userService");
    }
    
    public User getUser(Long id) {
        Supplier<User> decoratedSupplier = Decorators
            .ofSupplier(() -> fetchUser(id))
            .withCircuitBreaker(circuitBreaker)
            .withRetry(retry)
            .decorate();
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            return getFallbackUser(id);
        }
    }
    
    private User fetchUser(Long id) {
        // Actual API call
        return new User();
    }
    
    private User getFallbackUser(Long id) {
        return new User(id, "Fallback", "fallback@example.com");
    }
}

// Circuit Breaker events
@Component
public class CircuitBreakerEventListener {
    
    @EventListener
    public void onCircuitBreakerEvent(CircuitBreakerEvent event) {
        log.info("CircuitBreaker Event: {} - {}", 
                event.getEventType(), 
                event.getCircuitBreakerName());
        
        if (event.getEventType() == CircuitBreakerEvent.Type.ERROR) {
            // Send alert
        }
    }
}

// Health indicator integration
@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Autowired
    public CircuitBreakerHealthIndicator(
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            String name = cb.getName();
            CircuitBreaker.State state = cb.getState();
            details.put(name, state.toString());
        });
        
        boolean anyOpen = circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .anyMatch(cb -> cb.getState() == CircuitBreaker.State.OPEN);
        
        if (anyOpen) {
            return Health.down().withDetails(details).build();
        }
        
        return Health.up().withDetails(details).build();
    }
}
```

---

## 8. Messaging

### RabbitMQ

**Message Queue Integration:**

```java
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.*;

// RabbitMQ Configuration
@Configuration
public class RabbitMQConfig {
    
    // Direct Exchange
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("direct.exchange");
    }
    
    // Topic Exchange
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("topic.exchange");
    }
    
    // Fanout Exchange
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout.exchange");
    }
    
    // Queues
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("order.queue")
            .withArgument("x-message-ttl", 60000)
            .withArgument("x-max-length", 1000)
            .build();
    }
    
    @Bean
    public Queue notificationQueue() {
        return new Queue("notification.queue", true);
    }
    
    @Bean
    public Queue emailQueue() {
        return new Queue("email.queue", true);
    }
    
    @Bean
    public Queue smsQueue() {
        return new Queue("sms.queue", true);
    }
    
    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("dead.letter.queue", true);
    }
    
    @Bean
    public Queue mainQueueWithDLQ() {
        return QueueBuilder.durable("main.queue")
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "dead.letter.queue")
            .build();
    }
    
    // Bindings
    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(orderQueue)
            .to(directExchange)
            .with("order.routing.key");
    }
    
    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(emailQueue)
            .to(topicExchange)
            .with("notification.email.#");
    }
    
    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(smsQueue)
            .to(topicExchange)
            .with("notification.sms.#");
    }
    
    @Bean
    public Binding fanoutBinding1(Queue notificationQueue, 
                                  FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(notificationQueue).to(fanoutExchange);
    }
    
    // Message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setReturnsCallback(returnedMessage -> {
            log.error("Message returned: {}", returnedMessage.getMessage());
        });
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message not acknowledged: {}", cause);
            }
        });
        return template;
    }
}

// Publisher
@Service
public class MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    // Simple send
    public void sendOrderMessage(Order order) {
        rabbitTemplate.convertAndSend(
            "direct.exchange",
            "order.routing.key",
            order
        );
    }
    
    // Send with correlation ID
    public void sendOrderWithCorrelation(Order order, String correlationId) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(correlationId);
        messageProperties.setContentType("application/json");
        
        Message message = rabbitTemplate.getMessageConverter()
            .toMessage(order, messageProperties);
        
        rabbitTemplate.send("direct.exchange", "order.routing.key", message);
    }
    
    // Send with custom headers
    public void sendWithHeaders(Order order, Map<String, Object> headers) {
        rabbitTemplate.convertAndSend(
            "direct.exchange",
            "order.routing.key",
            order,
            message -> {
                MessageProperties props = message.getMessageProperties();
                headers.forEach(props::setHeader);
                return message;
            }
        );
    }
    
    // Topic exchange publish
    public void sendNotification(String type, String message) {
        String routingKey = "notification." + type + ".alert";
        rabbitTemplate.convertAndSend("topic.exchange", routingKey, message);
    }
    
    // Fanout exchange publish
    public void broadcastMessage(String message) {
        rabbitTemplate.convertAndSend("fanout.exchange", "", message);
    }
}

// Consumer
@Component
public class MessageConsumer {
    
    @RabbitListener(queues = "order.queue")
    public void handleOrderMessage(Order order) {
        log.info("Received order: {}", order);
        processOrder(order);
    }
    
    // With Message object for metadata
    @RabbitListener(queues = "order.queue")
    public void handleOrderWithMetadata(Order order, Message message) {
        log.info("Received order: {}", order);
        log.info("Correlation ID: {}", 
                message.getMessageProperties().getCorrelationId());
        processOrder(order);
    }
    
    // Multiple queues
    @RabbitListener(queues = {"email.queue", "sms.queue"})
    public void handleNotifications(String message,
                                   @Header("amqp_receivedRoutingKey") String routingKey) {
        log.info("Received notification via {}: {}", routingKey, message);
    }
    
    // Manual acknowledgment
    @RabbitListener(queues = "order.queue", ackMode = "MANUAL")
    public void handleOrderManualAck(Order order, Channel channel, 
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            processOrder(order);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            try {
                // Requeue if processing fails
                channel.basicNack(tag, false, true);
            } catch (IOException ioException) {
                log.error("Error while rejecting message", ioException);
            }
        }
    }
    
    // Error handling
    @RabbitListener(queues = "order.queue")
    public void handleOrderWithErrorHandling(Order order) {
        try {
            processOrder(order);
        } catch (Exception e) {
            log.error("Error processing order", e);
            throw new AmqpRejectAndDontRequeueException("Processing failed", e);
        }
    }
    
    // Dead Letter Queue listener
    @RabbitListener(queues = "dead.letter.queue")
    public void handleDeadLetter(Message message) {
        log.error("Message sent to DLQ: {}", message);
        // Alert, store for manual processing, etc.
    }
}

// Dynamic queue creation and binding
@Service
public class DynamicQueueService {
    
    private final AmqpAdmin amqpAdmin;
    
    @Autowired
    public DynamicQueueService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }
    
    public void createQueueAndBinding(String queueName, String exchangeName, 
                                     String routingKey) {
        Queue queue = new Queue(queueName, true);
        amqpAdmin.declareQueue(queue);
        
        Exchange exchange = new TopicExchange(exchangeName);
        amqpAdmin.declareExchange(exchange);
        
        Binding binding = BindingBuilder.bind(queue)
            .to((TopicExchange) exchange)
            .with(routingKey);
        amqpAdmin.declareBinding(binding);
    }
}
```

### Kafka

**Event Streaming with Kafka:**

```java
import org.springframework.kafka.core.*;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.support.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

// Kafka Configuration
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    // Producer Configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
                  StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                  JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                  StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                  JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> 
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties()
            .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}

// Producer
@Service
public class KafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    // Simple send
    public void sendMessage(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
    
    // Send with key
    public void sendMessageWithKey(String topic, String key, Object message) {
        kafkaTemplate.send(topic, key, message);
    }
    
    // Send with callback
    public void sendMessageAsync(String topic, Object message) {
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully: offset={}", 
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message", ex);
            }
        });
    }
    
    // Send with headers
    public void sendWithHeaders(String topic, Object message, 
                               Map<String, String> headers) {
        ProducerRecord<String, Object> record = 
            new ProducerRecord<>(topic, message);
        
        headers.forEach((key, value) -> 
            record.headers().add(key, value.getBytes()));
        
        kafkaTemplate.send(record);
    }
    
    // Transactional send
    @Transactional("kafkaTransactionManager")
    public void sendTransactional(String topic, List<Object> messages) {
        messages.forEach(message -> kafkaTemplate.send(topic, message));
    }
}

// Consumer
@Component
public class KafkaConsumer {
    
    @KafkaListener(topics = "order-topic", groupId = "order-consumer-group")
    public void consumeOrder(Order order) {
        log.info("Consumed order: {}", order);
        processOrder(order);
    }
    
    // With partition and offset info
    @KafkaListener(topics = "order-topic")
    public void consumeWithMetadata(
            Order order,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Consumed from partition {}, offset {}: {}", 
                partition, offset, order);
    }
    
    // Manual acknowledgment
    @KafkaListener(topics = "order-topic", 
                  containerFactory = "kafkaListenerContainerFactory")
    public void consumeWithManualAck(Order order, Acknowledgment ack) {
        try {
            processOrder(order);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing order", e);
            // Don't acknowledge - message will be reprocessed
        }
    }
    
    // Batch consumption
    @KafkaListener(topics = "order-topic")
    public void consumeBatch(List<Order> orders) {
        log.info("Consumed batch of {} orders", orders.size());
        orders.forEach(this::processOrder);
    }
    
    // Multiple topics
    @KafkaListener(topics = {"topic1", "topic2", "topic3"})
    public void consumeMultipleTopics(String message) {
        log.info("Consumed: {}", message);
    }
    
    // Topic pattern
    @KafkaListener(topicPattern = "order-.*")
    public void consumeTopicPattern(String message) {
        log.info("Consumed: {}", message);
    }
    
    // Error handling
    @KafkaListener(topics = "order-topic")
    public void consumeWithErrorHandler(Order order) {
        try {
            processOrder(order);
        } catch (Exception e) {
            log.error("Error processing order", e);
            throw e; // Will trigger error handler
        }
    }
}

// Error Handler
@Component
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {
    
    @Override
    public Object handleError(Message<?> message, 
                            ListenerExecutionFailedException exception,
                            Consumer<?, ?> consumer) {
        log.error("Error in consumer", exception);
        
        // Send to DLT (Dead Letter Topic)
        // Or implement retry logic
        
        return null;
    }
}

// Kafka Streams
@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, 
                 Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, 
                 Serdes.String().getClass());
        return new KafkaStreamsConfiguration(props);
    }
    
    @Bean
    public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder
            .stream("input-topic");
        
        // Transform and filter
        stream
            .filter((key, value) -> value.length() > 5)
            .mapValues(value -> value.toUpperCase())
            .to("output-topic");
        
        // Aggregate
        stream
            .groupByKey()
            .count()
            .toStream()
            .to("count-topic");
        
        return stream;
    }
}

// Advanced: Event Sourcing with Kafka
@Service
public class EventSourcingService {
    
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    public void publishEvent(Event event) {
        String aggregateId = event.getAggregateId();
        kafkaTemplate.send("events-topic", aggregateId, event);
    }
    
    @KafkaListener(topics = "events-topic")
    public void handleEvent(Event event) {
        // Update read model
        // Update projections
        // Trigger side effects
    }
}
```

---

## 9. DevOps & Deployment

### Docker

**Containerization:**

```dockerfile
# Multi-stage Dockerfile for Spring Boot
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: postgres
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
    networks:
      - app-network

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: user
      RABBITMQ_DEFAULT_PASS: password
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - app-network

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: spring-app
    depends_on:
      - postgres
      - redis
      - rabbitmq
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/myapp
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_REDIS_HOST: redis
      SPRING_RABBITMQ_HOST: rabbitmq
    ports:
      - "8080:8080"
    networks:
      - app-network
    restart: unless-stopped

volumes:
  postgres-data:

networks:
  app-network:
    driver: bridge
```

### Kubernetes

**Kubernetes Deployment:**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-app
  namespace: production
  labels:
    app: spring-app
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-app
  template:
    metadata:
      labels:
        app: spring-app
        version: v1
    spec:
      containers:
      - name: spring-app
        image: myregistry/spring-app:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        volumeMounts:
        - name: config
          mountPath: /config
          readOnly: true
      volumes:
      - name: config
        configMap:
          name: app-config

---
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: spring-app-service
  namespace: production
spec:
  type: LoadBalancer
  selector:
    app: spring-app
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
    name: http

---
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: spring-app-ingress
  namespace: production
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api.example.com
    secretName: api-tls
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: spring-app-service
            port:
              number: 80

---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: production
data:
  application.yml: |
    server:
      port: 8080
    spring:
      application:
        name: spring-app
      jpa:
        show-sql: false

---
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
  namespace: production
type: Opaque
stringData:
  url: jdbc:postgresql://postgres:5432/myapp
  username: user
  password: secretpassword

---
# hpa.yaml (Horizontal Pod Autoscaler)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: spring-app-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: spring-app
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### CI/CD

**GitLab CI/CD Pipeline:**

```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  DOCKER_REGISTRY: registry.example.com
  APP_NAME: spring-app

cache:
  paths:
    - .m2/repository

test:
  stage: test
  image: maven:3.8.4-openjdk-17
  script:
    - mvn clean test
    - mvn verify sonar:sonar
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/
  only:
    - merge_requests
    - develop
    - main

build:
  stage: build
  image: maven:3.8.4-openjdk-17
  script:
    - mvn clean package -DskipTests
    - docker build -t $DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_SHA .
    - docker push $DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_SHA
  only:
    - main
    - develop

deploy-staging:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/$APP_NAME $APP_NAME=$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_SHA -n staging
    - kubectl rollout status deployment/$APP_NAME -n staging
  environment:
    name: staging
    url: https://staging.example.com
  only:
    - develop

deploy-production:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/$APP_NAME $APP_NAME=$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_SHA -n production
    - kubectl rollout status deployment/$APP_NAME -n production
  environment:
    name: production
    url: https://api.example.com
  when: manual
  only:
    - main
```

**Jenkins Pipeline:**

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.8.4'
        jdk 'JDK 17'
    }
    
    environment {
        DOCKER_REGISTRY = 'registry.example.com'
        APP_NAME = 'spring-app'
        SONAR_HOST = 'https://sonarqube.example.com'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: 'target/*.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}")
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials') {
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}").push()
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}").push('latest')
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    kubernetesDeploy(
                        configs: 'k8s/staging/*.yaml',
                        kubeconfigId: 'kubeconfig-staging'
                    )
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                script {
                    kubernetesDeploy(
                        configs: 'k8s/production/*.yaml',
                        kubeconfigId: 'kubeconfig-production'
                    )
                }
            }
        }
    }
    
    post {
        success {
            slackSend(
                color: 'good',
                message: "Build ${BUILD_NUMBER} succeeded: ${JOB_NAME}"
            )
        }
        failure {
            slackSend(
                color: 'danger',
                message: "Build ${BUILD_NUMBER} failed: ${JOB_NAME}"
            )
        }
    }
}
```

### Cloud Deployment

**AWS Configuration:**

```java
// AWS S3 Configuration
@Configuration
public class AwsS3Config {
    
    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.access-key}")
    private String accessKey;
    
    @Value("${aws.secret-key}")
    private String secretKey;
    
    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(
            accessKey, secretKey);
        
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();
    }
}

@Service
public class S3Service {
    
    private final AmazonS3 s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    public void uploadFile(String key, File file) {
        s3Client.putObject(bucketName, key, file);
    }
    
    public InputStream downloadFile(String key) {
        S3Object object = s3Client.getObject(bucketName, key);
        return object.getObjectContent();
    }
    
    public void deleteFile(String key) {
        s3Client.deleteObject(bucketName, key);
    }
}

// AWS SQS Configuration
@Configuration
public class AwsSqsConfig {
    
    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    }
}

@Service
public class SqsService {
    
    private final AmazonSQS sqsClient;
    
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;
    
    public void sendMessage(String message) {
        SendMessageRequest request = new SendMessageRequest()
            .withQueueUrl(queueUrl)
            .withMessageBody(message);
        sqsClient.sendMessage(request);
    }
    
    public List<Message> receiveMessages() {
        ReceiveMessageRequest request = new ReceiveMessageRequest()
            .withQueueUrl(queueUrl)
            .withMaxNumberOfMessages(10)
            .withWaitTimeSeconds(20);
        
        return sqsClient.receiveMessage(request).getMessages();
    }
}
```

---

This completes the comprehensive guide covering all topics from the Java Spring Boot Developer Roadmap!
