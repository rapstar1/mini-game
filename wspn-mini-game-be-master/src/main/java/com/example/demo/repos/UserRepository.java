package com.example.demo.repos;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.models.User;


@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    User findByTelegramId(String telegramId);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.energy = u.energy + ?2 WHERE u.energy < ?1")
    void incrementEnergyForAllUsersWithLessThanMaxEnergy(int maxEnergy, int increment);
}
