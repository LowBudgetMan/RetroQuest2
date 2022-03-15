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

import * as React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { axe } from 'jest-axe';
import { RecoilRoot } from 'recoil';

import * as Modal from '../../../../../Common/Modal/Modal';
import ActionItemService from '../../../../../Services/Api/ActionItemService';
import { TeamState } from '../../../../../State/TeamState';
import Action from '../../../../../Types/Action';
import Team from '../../../../../Types/Team';

import ActionItem from './ActionItem';

export const mockUseModalValue = {
	hide: jest.fn(),
	show: jest.fn(),
	setHideOnEscape: jest.fn(),
	setHideOnBackdropClick: jest.fn(),
};

jest.spyOn(Modal, 'useModal').mockReturnValue(mockUseModalValue);

jest.mock('../../../../../Services/Api/ActionItemService');

describe('ActionItem', () => {
	const fadeInAnimationClass = 'fade-in';
	const fadeOutAnimationClass = 'fade-out';

	const team: Team = {
		name: 'My Team',
		id: 'my-team',
	};

	const fakeAction: Action = {
		id: 0,
		task: 'fake task',
		assignee: '',
		completed: false,
		dateCreated: '2021-08-12',
		archived: false,
	};

	beforeEach(() => {
		jest.clearAllMocks();
	});

	it('should render without axe errors', async () => {
		const { container } = render(
			<RecoilRoot>
				<ActionItem action={fakeAction} />
			</RecoilRoot>
		);
		const results = await axe(container);
		expect(results).toHaveNoViolations();
	});

	it('should render as an action column item with created date', () => {
		render(
			<RecoilRoot>
				<ActionItem action={fakeAction} />
			</RecoilRoot>
		);

		expect(screen.getByTestId('actionItem').className).toContain('action');
		screen.getByText('Aug 12th');
	});

	it('should disable animations', () => {
		render(
			<RecoilRoot>
				<ActionItem action={fakeAction} disableAnimations={true} />
			</RecoilRoot>
		);

		const actionItem = screen.getByTestId('actionItem');
		expect(actionItem.className).not.toContain(fadeInAnimationClass);
		expect(actionItem.className).not.toContain(fadeOutAnimationClass);
	});

	describe('When not completed and not readonly', () => {
		beforeEach(() => {
			render(
				<RecoilRoot
					initializeState={({ set }) => {
						set(TeamState, team);
					}}
				>
					<ActionItem action={fakeAction} />
				</RecoilRoot>
			);
		});

		it('should open retro item modal', () => {
			clickTask();

			expect(screen.getByTestId('actionItemModal')).toBeDefined();
		});

		it('should start and cancel editing of action item', () => {
			const newTask = 'New Fake Task';

			screen.getByText(fakeAction.task);

			clickEdit();
			screen.getByText(fakeAction.task);

			editTask(newTask);
			screen.getByText(newTask);

			hitEscapeKey();
			screen.getByText(fakeAction.task);

			clickEdit();
			screen.getByText(fakeAction.task);

			editTask(newTask);
			screen.getByText(newTask);

			clickEdit();
			screen.getByText(fakeAction.task);
		});

		it('should disable other items while editing', () => {
			clickEdit();
			expect(taskReadonly()).toBeFalsy();

			expect(assigneeDisable()).not.toBeNull();

			clickDelete();
			expect(deleteMessage()).toBeFalsy();

			clickCheckbox();
			expect(ActionItemService.updateCompletionStatus).not.toHaveBeenCalled();
		});

		it('should edit action item', () => {
			clickEdit();
			const updatedTask = 'New Fake Task';
			editTask(`${updatedTask}{Enter}`);

			expect(ActionItemService.updateTask).toHaveBeenCalledWith(
				team.id,
				fakeAction.id,
				updatedTask
			);
		});

		it('should close delete confirmation overlay if user clicks escape', () => {
			clickDelete();
			expect(deleteMessage()).toBeTruthy();

			hitEscapeKey();
			expect(deleteMessage()).toBeFalsy();
		});

		it('should not delete thought user cancels deletion', () => {
			expect(deleteMessage()).toBeFalsy();
			clickDelete();
			expect(deleteMessage()).toBeTruthy();

			clickCancelDelete();
			expect(deleteMessage()).toBeFalsy();
			expect(ActionItemService.delete).not.toHaveBeenCalled();
		});

		it('should delete action item when user confirms deletion', () => {
			clickDelete();
			clickConfirmDelete();

			expect(ActionItemService.delete).toHaveBeenCalledWith(
				team.id,
				fakeAction.id
			);
		});

		it('should mark action item as completed and switch animation class', () => {
			const actionItem = screen.getByTestId('actionItem');
			expect(actionItem.className).toContain(fadeInAnimationClass);
			expect(actionItem.className).not.toContain(fadeOutAnimationClass);
			clickCheckbox();

			expect(ActionItemService.updateCompletionStatus).toHaveBeenCalledWith(
				team.id,
				fakeAction.id,
				true
			);
			expect(actionItem.className).not.toContain(fadeInAnimationClass);
			expect(actionItem.className).toContain(fadeOutAnimationClass);
		});

		it('should edit Assignee', () => {
			typeAssignee('FordLabs{enter}');

			expect(ActionItemService.updateAssignee).toHaveBeenCalledWith(
				team.id,
				fakeAction.id,
				'FordLabs'
			);
			expect(ActionItemService.updateAssignee).toBeCalledTimes(1);

			typeAssignee(' Team');
			clickTask();

			expect(ActionItemService.updateAssignee).toHaveBeenCalledWith(
				team.id,
				fakeAction.id,
				'FordLabs Team'
			);
			expect(ActionItemService.updateAssignee).toBeCalledTimes(2);
		});
	});

	describe('When completed', () => {
		beforeEach(() => {
			render(
				<RecoilRoot
					initializeState={({ set }) => {
						set(TeamState, team);
					}}
				>
					<ActionItem action={{ ...fakeAction, completed: true }} />
				</RecoilRoot>
			);
		});

		it('should disable edit button', () => {
			clickEdit();
			expect(taskReadonly()).toBeTruthy();
		});

		it('should not open modal', () => {
			clickTask();
			expect(screen.queryByTestId('actionItemModal')).toBeNull();
		});

		it('should not disable delete button', () => {
			clickDelete();
			expect(deleteMessage()).toBeTruthy();
		});

		it('should not disable checkbox button', () => {
			clickCheckbox();
			expect(ActionItemService.updateCompletionStatus).toHaveBeenCalledWith(
				team.id,
				fakeAction.id,
				false
			);
		});
	});

	describe('When readonly', () => {
		beforeEach(() => {
			render(
				<RecoilRoot
					initializeState={({ set }) => {
						set(TeamState, team);
					}}
				>
					<ActionItem action={fakeAction} readOnly={true} />
				</RecoilRoot>
			);
		});

		it('should disable all buttons', () => {
			clickEdit();
			expect(taskReadonly()).toBeTruthy();

			clickDelete();
			expect(deleteMessage()).toBeFalsy();

			clickCheckbox();
			expect(ActionItemService.updateCompletionStatus).not.toHaveBeenCalled();
		});

		it('should disable Assignee', () => {
			typeAssignee('new Assignee{Enter}');
			expect(ActionItemService.updateAssignee).not.toHaveBeenCalled();
		});

		it('should open retro item modal', () => {
			clickTask();
			expect(screen.getByTestId('actionItemModal')).toBeDefined();
		});
	});
});

function taskReadonly() {
	return screen.getByTestId('editableText').getAttribute('readonly') === '';
}

function assigneeDisable() {
	return screen.getByTestId('actionItem-assignee').getAttribute('disabled');
}

export function editTask(text: string) {
	const textArea = (screen.queryByTestId('editableText') ||
		screen.queryByTestId('addActionItem-task')) as HTMLTextAreaElement;
	textArea.select();
	userEvent.type(textArea, text);
}

function clickTask() {
	userEvent.click(screen.getByTestId('editableText-container'));
}

export function typeAssignee(text: string) {
	return userEvent.type(screen.getByTestId('actionItem-assignee'), text);
}

function clickEdit() {
	userEvent.click(screen.getByTestId('editButton'));
}

function clickDelete() {
	userEvent.click(screen.getByTestId('deleteButton'));
}

function clickCheckbox() {
	userEvent.click(screen.getByTestId('checkboxButton'));
}

function clickCancelDelete() {
	userEvent.click(screen.getByText('No'));
}

function clickConfirmDelete() {
	userEvent.click(screen.getByText('Yes'));
}

export function hitEscapeKey() {
	userEvent.type(document.body, '{Escape}');
}

function deleteMessage() {
	return screen.queryByText('Delete this Action Item?');
}
