## 2024-05-18 - Admin Invite Code Authorization Bypass
**Vulnerability:** Registration for `Constants.ROLE_ADMIN` relied only on client-side UI enabling of the submit button, allowing potential bypass of admin privilege restrictions at the ViewModel/API invocation level.
**Learning:** Client-side checks (such as disabling a button in `RegisterScreen`) are insufficient for authorization. Critical checks must be enforced at the ViewModel/business logic level prior to delegating to repositories or backend services.
**Prevention:** Always perform input and authorization validation in the ViewModel / domain layer before executing privileged operations.
