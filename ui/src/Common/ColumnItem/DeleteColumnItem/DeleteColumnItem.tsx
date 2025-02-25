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

import React, { useEffect, useRef } from 'react';
import classNames from 'classnames';
import {
	CancelButton,
	ColumnItemButtonGroup,
	ConfirmButton,
} from 'Common/ColumnItemButtons';
import { onKeys } from 'Utils/EventUtils';

import './DeleteColumnItem.scss';

interface DeletionOverlayProps {
	children: JSX.Element | string;
	onConfirm: () => void;
	onCancel: () => void;
	height?: number;
	className?: string;
}

function DeleteColumnItem(props: DeletionOverlayProps) {
	const { onConfirm, onCancel, children, height, className } = props;

	const cancelButtonRef = useRef<HTMLButtonElement>(null);

	useEffect(() => {
		cancelButtonRef.current?.focus();

		const escapeListener = onKeys('Escape', onCancel);
		document.addEventListener('keydown', escapeListener);

		return () => {
			document.removeEventListener('keydown', escapeListener);
		};
	}, [onCancel]);

	return (
		<div
			className={classNames('delete-column-item thought-item', className)}
			data-testid="deleteColumnItem"
			style={{ height }}
		>
			<div className="delete-column-item-message">{children}</div>
			<ColumnItemButtonGroup>
				<CancelButton onClick={onCancel} ref={cancelButtonRef}>
					Cancel
				</CancelButton>
				<ConfirmButton onClick={onConfirm}>Yes, Delete</ConfirmButton>
			</ColumnItemButtonGroup>
		</div>
	);
}

export default DeleteColumnItem;
