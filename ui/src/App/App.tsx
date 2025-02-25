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

import React, { useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Modal from 'Common/Modal/Modal';
import useEnvironmentConfig from 'Hooks/useEnvironmentConfig';
import { useRecoilValue } from 'recoil';
import { getThemeClassFromUserSettings, ThemeState } from 'State/ThemeState';

import {
	EXPIRED_EMAIL_RESET_LINK_PATH,
	EXPIRED_PASSWORD_RESET_LINK_PATH,
	PASSWORD_RESET_REQUEST_PATH,
	RECOVER_TEAM_NAME_PATH,
} from '../RouteConstants';

import ChangeTeamDetailsPage from './ChangeTeamDetails/ChangeTeamDetailsPage';
import CreateTeamPage from './CreateTeam/CreateTeamPage';
import ExpiredResetBoardOwnersLinkPage from './ExpiredResetBoardOwnersLinkPage/ExpiredResetBoardOwnersLinkPage';
import ExpiredResetPasswordLinkPage from './ExpiredResetPasswordLinkPage/ExpiredResetPasswordLinkPage';
import LoginPage from './Login/LoginPage';
import PageNotFoundPage from './PageNotFound/PageNotFoundPage';
import PasswordResetRequestPage from './PasswordResetRequest/PasswordResetRequestPage';
import RecoverTeamNamePage from './RecoverTeamName/RecoverTeamNamePage';
import ResetPasswordPage from './ResetPassword/ResetPasswordPage';
import ActionItemArchives from './Team/Archives/ActionItemArchives/ActionItemArchives';
import ArchivesPage from './Team/Archives/ArchivesPage';
import ArchivedBoard from './Team/Archives/ThoughtArchives/ArchivedBoard/ArchivedBoard';
import ArchivedBoardsList from './Team/Archives/ThoughtArchives/ArchivedBoardsList/ArchivedBoardsList';
import RadiatorPage from './Team/Radiator/RadiatorPage';
import RetroPage from './Team/Retro/RetroPage';
import TeamPages from './Team/TeamPages';

import '@fortawesome/fontawesome-free/css/all.css';
import '../Styles/main.scss';
import './App.scss';

function App() {
	const theme = useRecoilValue(ThemeState);

	const environmentConfig = useEnvironmentConfig();
	const { email_is_enabled = true } = environmentConfig || {};

	useEffect(() => {
		document.body.className = getThemeClassFromUserSettings();
	}, [theme]);

	return (
		<div className="retroquest-app" data-testid="retroquest-app">
			<Routes>
				<Route path="/" element={<Navigate replace to="/login" />} />
				<Route path="/login" element={<LoginPage />} />
				<Route path="/login/:teamId" element={<LoginPage />} />
				<Route path="/create" element={<CreateTeamPage />} />
				<Route path="/team/:teamId" element={<TeamPages />}>
					<Route path="" element={<RetroPage />} />
					<Route path="archives" element={<ArchivesPage />}>
						<Route path="" element={<Navigate replace to="thoughts" />} />
						<Route path="thoughts" element={<ArchivedBoardsList />} />
						<Route path="thoughts/:boardId" element={<ArchivedBoard />} />
						<Route path="action-items" element={<ActionItemArchives />} />
					</Route>
					<Route path="radiator" element={<RadiatorPage />} />
				</Route>
				{email_is_enabled && (
					<Route path="/email/reset" element={<ChangeTeamDetailsPage />} />
				)}
				{email_is_enabled && (
					<Route path="/password/reset" element={<ResetPasswordPage />} />
				)}
				{email_is_enabled && (
					<Route
						path={EXPIRED_PASSWORD_RESET_LINK_PATH}
						element={<ExpiredResetPasswordLinkPage />}
					/>
				)}
				{email_is_enabled && (
					<Route
						path={EXPIRED_EMAIL_RESET_LINK_PATH}
						element={<ExpiredResetBoardOwnersLinkPage />}
					/>
				)}
				{email_is_enabled && (
					<Route
						path={PASSWORD_RESET_REQUEST_PATH}
						element={<PasswordResetRequestPage />}
					/>
				)}
				{email_is_enabled && (
					<Route
						path={RECOVER_TEAM_NAME_PATH}
						element={<RecoverTeamNamePage />}
					/>
				)}
				<Route path="*" element={<PageNotFoundPage />} />
			</Routes>
			<Modal />
		</div>
	);
}

export default App;
