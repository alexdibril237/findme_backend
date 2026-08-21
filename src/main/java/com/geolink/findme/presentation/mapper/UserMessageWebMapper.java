package com.geolink.findme.presentation.mapper;

import com.geolink.findme.data.entity.UserMessage;
import com.geolink.findme.presentation.dto.UserMessageResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMessageWebMapper {

    public UserMessageResponse toResponse(UserMessage message) {
        return new UserMessageResponse(
                message.getId(),
                message.getSubject(),
                message.getBody(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}
