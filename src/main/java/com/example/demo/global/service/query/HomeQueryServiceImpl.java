package com.example.demo.global.service.query;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.web.dto.HomeResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeQueryServiceImpl implements HomeQueryService {

    private final UserCharacterFavoriteRepository userCharacterFavoriteRepository;

    @Override
    @Transactional
    public HomeResponse getHomeData(User user) {

        // 유저의 즐겨찾기 캐릭터 최대 5개 가져오기
        List<StoryCharacter> favorites = userCharacterFavoriteRepository
                .findTop5ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(fav -> fav.getCharacter())
                .toList();

        return HomeResponse.from(user, favorites);
    }

}