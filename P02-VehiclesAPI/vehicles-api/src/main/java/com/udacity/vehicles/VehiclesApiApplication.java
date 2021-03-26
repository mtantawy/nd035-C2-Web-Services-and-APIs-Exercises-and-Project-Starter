package com.udacity.vehicles;

import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Launches a Spring Boot application for the Vehicles API,
 * initializes the car manufacturers in the database,
 * and launches web clients to communicate with maps and pricing.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableEurekaClient
public class VehiclesApiApplication {
    @Autowired
    private DiscoveryClient discoveryClient;

    public static void main(String[] args) {
        SpringApplication.run(VehiclesApiApplication.class, args);
    }

    /**
     * Initializes the car manufacturers available to the Vehicle API.
     * @param repository where the manufacturer information persists.
     * @return the car manufacturers to add to the related repository
     */
    @Bean
    CommandLineRunner initDatabase(ManufacturerRepository repository) {
        return args -> {
            repository.save(new Manufacturer(100, "Audi"));
            repository.save(new Manufacturer(101, "Chevrolet"));
            repository.save(new Manufacturer(102, "Ford"));
            repository.save(new Manufacturer(103, "BMW"));
            repository.save(new Manufacturer(104, "Dodge"));
        };
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Web Client for the maps (location) API
     * @param serviceId Id of the service in Eureka to look for
     * @return created maps endpoint
     */
    @Bean(name="maps")
    public WebClient webClientMaps(@Value("${maps.serviceId}") String serviceId) {
        return getWebClientForUri(serviceId);
    }

    private WebClient getWebClientForUri(@Value("${maps.serviceId}") String serviceId) {
        ServiceInstance serviceInstance =
                this.discoveryClient.getInstances(serviceId).stream().findFirst().orElseThrow(RuntimeException::new);
        return WebClient.create(serviceInstance.getUri().toString());
    }

    /**
     * Web Client for the pricing API
     * @param serviceId Id of the service in Eureka to look for
     * @return created pricing endpoint
     */
    @Bean(name="pricing")
    public WebClient webClientPricing(@Value("${pricing.serviceId}") String serviceId) {
        return getWebClientForUri(serviceId);
    }

    @RestController
    static
    class ServiceInstanceRestController {

        @Autowired
        private DiscoveryClient discoveryClient;

        @RequestMapping("/service-instances/{applicationName}")
        public List<ServiceInstance> serviceInstancesByApplicationName(
                @PathVariable String applicationName) {
            return this.discoveryClient.getInstances(applicationName);
        }
    }

}
