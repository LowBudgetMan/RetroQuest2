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

import React from 'react';
import { useRecoilValue } from 'recoil';

import EditColumnItem from '../../../../../../Common/ColumnItem/EditColumnItem/EditColumnItem';
import ThoughtService from '../../../../../../Services/Api/ThoughtService';
import { TeamState } from '../../../../../../State/TeamState';
import Thought from '../../../../../../Types/Thought';
import { ThoughtItemViewState } from '../ThoughtItem';

interface Props {
	thought: Thought;
	setViewState: (viewState: ThoughtItemViewState) => void;
}

function EditThoughtView(props: Props) {
	const { thought, setViewState } = props;
	const team = useRecoilValue(TeamState);

	const updateThought = (updatedThoughtMessage: string) => {
		ThoughtService.updateMessage(
			team.id,
			thought.id,
			updatedThoughtMessage
		).catch(console.error);
	};

	const changeToDefaultView = () => setViewState(ThoughtItemViewState.DEFAULT);

	return (
		<EditColumnItem
			initialValue={thought.message}
			onCancel={changeToDefaultView}
			onConfirm={(updatedThought) => {
				updateThought(updatedThought);
				changeToDefaultView();
			}}
		/>
	);
}

export default EditThoughtView;
