/*
 * Copyright (c) 2022 Ford Motor Company
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ford.labs.retroquest.actionitem;

import com.ford.labs.retroquest.exception.ActionItemDoesNotExistException;
import com.ford.labs.retroquest.websocket.WebsocketService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ActionItemServiceTest {
    private final ActionItemRepository mockActionItemRepository = mock(ActionItemRepository.class);
    private final WebsocketService mockWebsocketService = mock(WebsocketService.class);
    private final ActionItemService actionItemService = new ActionItemService(mockActionItemRepository, mockWebsocketService);

    @Test
    public void archiveCompletedActionItems_MarksCompletedButUnarchivedActionItemsAsArchived() {
        var completedActionItem = ActionItem.builder().id(1L).teamId("The team").completed(true).archived(false).build();
        var otherCompletedActionItem = ActionItem.builder().id(2L).teamId("The team").completed(true).archived(false).build();
        when(mockActionItemRepository.findAllByTeamIdAndArchivedIsFalseAndCompletedIsTrue("The team")).thenReturn(List.of(completedActionItem, otherCompletedActionItem));

        actionItemService.archiveCompletedActionItems("The team");

        var expectedCompletedActionItem = ActionItem.builder().id(1L).teamId("The team").completed(true).archived(true).build();
        var otherExpectedCompletedActionItem = ActionItem.builder().id(2L).teamId("The team").completed(true).archived(true).build();
        verify(mockActionItemRepository).saveAll(List.of(expectedCompletedActionItem, otherExpectedCompletedActionItem));
    }

    @Test
    void fetchActionItem_ReturnsActionItemFromRepository() {
        var expectedActionItem = Optional.of(new ActionItem());
        when(mockActionItemRepository.findByTeamIdAndId("teamId", 1L)).thenReturn(expectedActionItem);
        var actual = actionItemService.fetchActionItem("teamId", 1L);
        assertThat(actual).isEqualTo(expectedActionItem.get());
    }

    @Test
    void fetchActionItem_WhenOptionalIsEmpty_ThrowsException() {
        when(mockActionItemRepository.findByTeamIdAndId("teamId", 1L)).thenThrow(new ActionItemDoesNotExistException());
        assertThatThrownBy(() -> actionItemService.fetchActionItem("teamId", 1L)).isInstanceOf(ActionItemDoesNotExistException.class);
    }

}