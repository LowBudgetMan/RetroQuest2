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

import * as React from 'react';
import classNames from 'classnames';

import './ArchivesSubheader.scss';

interface Props {
  showActionItems: boolean;
  setShowActionItems(showActionItems: boolean): void;
}

function ArchivesSubheader(props: Props): JSX.Element {
  const { showActionItems, setShowActionItems } = props;

  return (
    <div className="archives-subheader subheader">
      <ul className="archives-subheader-links">
        <li>
          <button
            className={classNames('button button-secondary', { active: !showActionItems })}
            onClick={() => setShowActionItems(false)}
          >
            Thoughts
          </button>
        </li>
        <li>
          <button
            className={classNames('button button-secondary', { active: !!showActionItems })}
            onClick={() => setShowActionItems(true)}
          >
            Action Items
          </button>
        </li>
      </ul>
    </div>
  );
}

export default ArchivesSubheader;
