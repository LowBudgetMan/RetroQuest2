/*
 * Copyright (c) 2018 Ford Motor Company
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

import {emptyActionItem} from '../../teams/domain/action-item';
import {ActionItemTaskComponent} from './action-item-task.component';

describe('ThoughtComponent', () => {
  let component: ActionItemTaskComponent;

  beforeEach(() => {
    component = new ActionItemTaskComponent();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('toggleEditMode', () => {

    let originalTimeoutFunction = null;
    const fakeElementRef = {
      nativeElement: jasmine.createSpyObj({
        focus: null
      })
    };

    beforeEach(() => {
      originalTimeoutFunction = window.setTimeout;
      window.setTimeout = (fn) => fn();

      component.editableTextArea = fakeElementRef;
    });

    afterEach(() => {
      window.setTimeout = originalTimeoutFunction;
      fakeElementRef.nativeElement.focus.calls.reset();
    });

    it('should set the edit mode value to true', () => {
      component.taskEditModeEnabled = false;
      component.toggleEditMode();

      expect(component.taskEditModeEnabled).toBeTruthy();
    });

    it('should set the edit mode value to false', () => {
      component.taskEditModeEnabled = true;
      component.toggleEditMode();

      expect(component.taskEditModeEnabled).toBeFalsy();
    });

    it('should focus the title area when the edit mode is toggled true', () => {
      component.taskEditModeEnabled = false;
      component.toggleEditMode();
      expect(component.editableTextArea.nativeElement.focus).toHaveBeenCalled();
    });

    it('should not focus the title area when the edit mode is toggled false', () => {
      component.taskEditModeEnabled = true;
      component.toggleEditMode();
      expect(component.editableTextArea.nativeElement.focus).not.toHaveBeenCalled();
    });

    it('should select all the text in the div when focused', () => {
      const originalExecCommand = document.execCommand;

      const mockExecCommand = spyOn(document, 'execCommand');

      component.taskEditModeEnabled = false;
      component.toggleEditMode();
      expect(mockExecCommand).toHaveBeenCalled();

      document.execCommand = originalExecCommand;
    });

    it('should not select all the text in the div when not focused', () => {
      const originalExecCommand = document.execCommand;

      const mockExecCommand = spyOn(document, 'execCommand');

      component.taskEditModeEnabled = true;
      component.toggleEditMode();
      expect(mockExecCommand).not.toHaveBeenCalled();

      document.execCommand = originalExecCommand;
    });
  });

  describe('emitDeleteItem', () => {

    it('should emit the actionItem to be deleted', () => {
      component.deleted = jasmine.createSpyObj({emit: null});

      component.actionItem = emptyActionItem();
      component.actionItem.task = 'FAKE TASK';
      component.emitDeleteItem();

      expect(component.deleted.emit).toHaveBeenCalledWith(component.actionItem);
    });

  });

  describe('emitTaskContentClicked', () => {

    it('should emit the actionItem when edit mode is not enabled', () => {
      component.taskEditModeEnabled = false;
      component.messageClicked = jasmine.createSpyObj({emit: null});

      component.actionItem = emptyActionItem();
      component.actionItem.task = 'FAKE TASK';
      component.emitTaskContentClicked();

      expect(component.messageClicked.emit).toHaveBeenCalledWith(component.actionItem);
    });

    it('should not emit the actionItem when edit mode is enabled', () => {
      component.taskEditModeEnabled = true;
      component.messageClicked = jasmine.createSpyObj({emit: null});

      component.actionItem = emptyActionItem();
      component.actionItem.task = 'FAKE TASK';
      component.emitTaskContentClicked();

      expect(component.messageClicked.emit).not.toHaveBeenCalled();
    });

  });

  describe('forceBlur', () => {
    let originalTimeoutFunction = null;

    beforeEach(() => {
      originalTimeoutFunction = window.setTimeout;
      window.setTimeout = (fn) => fn();
    });

    afterEach(() => {
      window.setTimeout = originalTimeoutFunction;
    });

    it('should call blur on the native textarea element', () => {
      component.editableTextArea = {
        nativeElement: jasmine.createSpyObj({
          blur: null
        })
      };
      component.forceBlur();

      expect(component.editableTextArea.nativeElement.blur).toHaveBeenCalled();
    });
  });

  describe('getDateCreated', () => {
    it('should return formatted date', () => {
      component.actionItem = {
        id: 0,
        task: '',
        completed: false,
        teamId: '',
        assignee: '',
        expanded: false,
        dateCreated: '2018-08-08'
      };
      const dateString = component.getDateCreated();
      expect(dateString).toEqual('Aug 8th');
    });

    it('should return null for undefined or empty dates', () => {
      component.actionItem = {
        id: 0,
        task: '',
        completed: false,
        teamId: '',
        assignee: '',
        expanded: false,
        dateCreated: undefined
      };
      const dateString = component.getDateCreated();
      expect(dateString).toEqual('—');
    });
  });
});
