package com.JobAyong.repository;

import com.JobAyong.entity.Voice;
import com.JobAyong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoiceRepository extends JpaRepository<Voice, Long> {
    List<Voice> findByUser(User user);
    List<Voice> findByUserEmail(String email);
} 