package com.tesis.motor_procesos.repository;

import com.tesis.motor_procesos.repository.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {


    Person findByUsername(String username);
}
