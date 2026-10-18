# Spring Security Integration Todo List

- [ ] Analyze security requirements (authentication & authorization detail: login, user storage, user roles, token/session type, etc.)
- [ ] Add Spring Security dependencies to pom.xml
- [ ] Create a SecurityConfig class for HTTP security and auth manager
- [ ] Implement a custom UserDetailsService based on UserEntity/UserRepository
- [ ] Add password encoding/validation
- [ ] Integrate login endpoint using LoginRequest DTO
- [ ] Map authentication to JWT/session (if required)
- [ ] Define authorization rules (e.g., per controller or method)
- [ ] Test login, authentication, and authorization flow
- [ ] Verify with user scenarios and adjust configs as needed
