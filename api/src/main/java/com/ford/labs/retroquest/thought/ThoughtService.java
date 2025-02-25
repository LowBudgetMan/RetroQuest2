/*
 * Copyright (c) 2021 Ford Motor Company
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

package com.ford.labs.retroquest.thought;

import com.ford.labs.retroquest.column.ColumnRepository;
import com.ford.labs.retroquest.exception.ColumnNotFoundException;
import com.ford.labs.retroquest.exception.ThoughtNotFoundException;
import com.ford.labs.retroquest.websocket.WebsocketService;
import com.ford.labs.retroquest.websocket.events.WebsocketThoughtEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.ford.labs.retroquest.websocket.events.WebsocketEventType.DELETE;
import static com.ford.labs.retroquest.websocket.events.WebsocketEventType.UPDATE;

@Service
public class ThoughtService {

    private final ThoughtRepository thoughtRepository;
    private final ColumnRepository columnRepository;
    private final WebsocketService websocketService;

    public ThoughtService(ThoughtRepository thoughtRepository, ColumnRepository columnRepository, WebsocketService websocketService) {
        this.thoughtRepository = thoughtRepository;
        this.columnRepository = columnRepository;
        this.websocketService = websocketService;
    }

    public List<Thought> fetchAllActiveThoughts(String teamId) {
        return thoughtRepository.findAllByTeamIdAndBoardIdIsNull(teamId);
    }

    public Optional<Thought> getThought(Long thoughtId) {
        return thoughtRepository.findById(thoughtId);
    }

    public Thought likeThought(String teamId, Long thoughtId) {
        thoughtRepository.incrementHeartCount(thoughtId);
        var thought = fetchThought(teamId, thoughtId);
        websocketService.publishEvent(new WebsocketThoughtEvent(thought.getTeamId(), UPDATE, thought));
        return thought;
    }

    public Thought discussThought(String teamId, Long thoughtId, boolean discussed) {
        var thought = fetchThought(teamId, thoughtId);
        thought.setDiscussed(discussed);
        var savedThought = thoughtRepository.save(thought);
        websocketService.publishEvent(new WebsocketThoughtEvent(savedThought.getTeamId(), UPDATE, savedThought));
        return savedThought;
    }

    public Thought updateColumn(String teamId, Long thoughtId, long columnId) {
        var column = columnRepository.findByTeamIdAndId(teamId, columnId).orElseThrow(ColumnNotFoundException::new);
        var thought = fetchThought(teamId, thoughtId);
        thought.setColumnId(column.getId());
        var savedThought = thoughtRepository.save(thought);
        websocketService.publishEvent(new WebsocketThoughtEvent(savedThought.getTeamId(), UPDATE, savedThought));
        return savedThought;
    }

    public Thought updateThoughtMessage(String teamId, Long thoughtId, String updatedMessage) {
        var returnedThought = fetchThought(teamId, thoughtId);
        returnedThought.setMessage(updatedMessage);
        var savedThought = thoughtRepository.save(returnedThought);
        websocketService.publishEvent(new WebsocketThoughtEvent(savedThought.getTeamId(), UPDATE, savedThought));
        return savedThought;
    }

    public void deleteThought(String teamId, Long thoughtId) {
        thoughtRepository.deleteThoughtByTeamIdAndId(teamId, thoughtId);
        websocketService.publishEvent(new WebsocketThoughtEvent(teamId, DELETE, Thought.builder().id(thoughtId).build()));
    }

    public Thought createThought(String teamId, CreateThoughtRequest request) {
        var thought = new Thought();
        thought.setMessage(request.message());
        thought.setColumnId(request.columnId());
        thought.setTeamId(teamId);

        Thought createdThought = thoughtRepository.save(thought);
        websocketService.publishEvent(new WebsocketThoughtEvent(teamId, UPDATE, createdThought));
        return createdThought;
    }

    private Thought fetchThought(String teamId, Long thoughtId) throws ThoughtNotFoundException {
        return thoughtRepository.findByTeamIdAndId(teamId, thoughtId).orElseThrow(() -> new ThoughtNotFoundException(thoughtId));
    }
}
