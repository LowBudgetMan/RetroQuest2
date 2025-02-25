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

import React, { useState } from 'react';
import AuthTemplate from 'Common/AuthTemplate/AuthTemplate';
import Form from 'Common/AuthTemplate/Form/Form';
import CheckYourMailConfirmationPage from 'Common/CheckYourMailConfirmationPage/CheckYourMailConfirmationPage';
import InputEmail from 'Common/InputEmail/InputEmail';
import useEnvironmentConfig from 'Hooks/useEnvironmentConfig';
import EmailService from 'Services/Api/EmailService';

function RecoverTeamNamePage() {
	const [recoveryEmail, setRecoveryEmail] = useState<string>('');
	const [errorMessages, setErrorMessages] = useState<string[]>([]);
	const [emailSent, setEmailSent] = useState<boolean>(false);

	const environmentConfig = useEnvironmentConfig();

	function onSubmit() {
		EmailService.sendTeamNameRecoveryEmail(recoveryEmail)
			.then(() => setEmailSent(true))
			.catch((error) => {
				setErrorMessages([error.response?.data?.message]);
			});
	}

	return (
		<>
			{!emailSent && (
				<AuthTemplate
					header="Recover Team Name"
					subHeader="Enter your email below, and we’ll send you the team name (or names!) registered to your email address."
					showGithubLink={false}
				>
					<Form
						submitButtonText="Send me my team name"
						onSubmit={onSubmit}
						disableSubmitBtn={!recoveryEmail}
						errorMessages={errorMessages}
					>
						<InputEmail
							value={recoveryEmail}
							onChange={setRecoveryEmail}
							validateInput={false}
						/>
					</Form>
				</AuthTemplate>
			)}
			{emailSent && (
				<CheckYourMailConfirmationPage
					paragraph1={`If any RetroQuest teams are registered to ${recoveryEmail}, we’ve sent a list of all team names that are connected to the email address.`}
					paragraph2={`If an email doesn’t show up soon, check your spam folder. We sent it from ${environmentConfig?.email_from_address}.`}
				/>
			)}
		</>
	);
}

export default RecoverTeamNamePage;
