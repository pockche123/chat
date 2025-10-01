package com.example.chatapp.repository;

import com.example.chatapp.model.UserDevice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends MongoRepository<UserDevice, String> {

    Optional<UserDevice> findUserDeviceByUserId(UUID uuid);
}
