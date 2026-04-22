package com.mapic.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveLocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String GEO_KEY = "mapic:users:location";

    /**
     * Update user location in Redis Geospatial index
     */
    public void updateUserLocation(Long userId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(longitude, latitude), String.valueOf(userId));
    }

    /**
     * Get specific user's location
     */
    public Point getUserLocation(Long userId) {
        List<Point> points = redisTemplate.opsForGeo().position(GEO_KEY, String.valueOf(userId));
        if (points != null && !points.isEmpty()) {
            return points.get(0);
        }
        return null;
    }

    /**
     * Get locations for a list of friend IDs
     */
    public List<UserLocationDTO> getFriendsLocations(List<Long> friendIds) {
        String[] members = friendIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        if (members.length == 0) return List.of();

        List<Point> points = redisTemplate.opsForGeo().position(GEO_KEY, (Object[]) members);
        
        // Match points with friend IDs (List size is identical to members size)
        java.util.ArrayList<UserLocationDTO> results = new java.util.ArrayList<>();
        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                if (p != null) {
                    results.add(new UserLocationDTO(
                            Long.parseLong(members[i]), 
                            p.getX(), // Longitude
                            p.getY()  // Latitude
                    ));
                }
            }
        }
        return results;
    }

    public record UserLocationDTO(Long userId, double longitude, double latitude) {}
}
