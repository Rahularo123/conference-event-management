package com.joyevents.platform.service;

import com.joyevents.platform.model.EPoster;
import com.joyevents.platform.repository.EPosterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EPosterService {

    private final EPosterRepository ePosterRepository;

    public EPosterService(EPosterRepository ePosterRepository) {
        this.ePosterRepository = ePosterRepository;
    }

    @Transactional(readOnly = true)
    public List<EPoster> getPosters(String conferenceCode, String query, String track) {
        if (query != null && !query.trim().isEmpty()) {
            return ePosterRepository.searchPosters(conferenceCode, query.trim());
        }
        if (track != null && !track.trim().isEmpty() && !"ALL".equalsIgnoreCase(track.trim())) {
            return ePosterRepository.findByConferenceCodeAndTrackOrderByPosterNumberAsc(conferenceCode, track.trim());
        }
        return ePosterRepository.findByConferenceCodeOrderByPosterNumberAsc(conferenceCode);
    }

    @Transactional(readOnly = true)
    public Optional<EPoster> getPosterById(Long id) {
        return ePosterRepository.findById(id);
    }

    @Transactional
    public EPoster recordView(Long id) {
        EPoster poster = ePosterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("E-Poster not found: " + id));
        poster.setViewCount(poster.getViewCount() + 1);
        return ePosterRepository.save(poster);
    }

    @Transactional
    public EPoster savePoster(EPoster poster) {
        return ePosterRepository.save(poster);
    }

    @Transactional
    public void deletePoster(Long id) {
        ePosterRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getTracks(String conferenceCode) {
        return ePosterRepository.findByConferenceCodeOrderByPosterNumberAsc(conferenceCode).stream()
                .map(EPoster::getTrack)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .sorted()
                .toList();
    }
}
