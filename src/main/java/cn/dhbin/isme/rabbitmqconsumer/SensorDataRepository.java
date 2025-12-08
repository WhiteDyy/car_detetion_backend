package cn.dhbin.isme.rabbitmqconsumer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
}
