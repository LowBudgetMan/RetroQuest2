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

import axios from 'axios';

import CreateThoughtRequest from '../../types/CreateThoughtRequest';

function getCreateThoughtResponse(teamId: string, topic: string, message: string): CreateThoughtRequest {
  return {
    id: -1,
    teamId,
    topic,
    message,
    hearts: 0,
    discussed: false,
  };
}

const ThoughtService = {
  createThought: (teamId: string, createThoughtRequest: CreateThoughtRequest) => {
    const url = `/api/team/${teamId}/thought`;
    return axios.post(url, createThoughtRequest).then((response) => response.data);
  },
};

export default ThoughtService;

export { getCreateThoughtResponse };
