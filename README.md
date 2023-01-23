
## country-by-country-reporting-frontend

---

### Intro
This service allows registered users (agent, client or organisation) to upload files on behalf of an organisation.

The backend to this service can be found [here](https://github.com/hmrc/country-by-country-reporting) which integrates with HOD, i.e. DES/ETMP

---


### Dependencies

| Service               | Link |
|-----------------------|------|
| Auth                  |https://github.com/hmrc/auth    |
| Country by country    |https://github.com/hmrc/country-by-country-reporting    |
| Tax enrolments        |https://github.com/hmrc/tax-enrolments    |
| Email                 |https://github.com/hmrc/email      |
| Assets frontend       |https://github.com/hmrc/assets-frontend      |
| User details          |https://github.com/hmrc/user-details      |
| Identity verification |https://github.com/hmrc/identity-verification      |
| Country by country reporting frontend |https://github.com/hmrc/country-by-country-reporting-frontend      |
| Country by country reporting stubs |https://github.com/hmrc/country-by-country-reporting-stubs      |
| Register country by country reporting frontend |https://github.com/hmrc/register-country-by-country-reporting-frontend      |
| Register country by country reporting frontend stubs |https://github.com/hmrc/register-country-by-country-reporting-stubs      |
| Register country by country reporting |https://github.com/hmrc/register-country-by-country-reporting      |

---


### Endpoints used

| Service             | HTTP Method | Route                                   | Purpose                                                          |
|---------------------|-------------|-----------------------------------------|------------------------------------------------------------------|
| Country-by-country-reporting        | POST        | /subscription/read-subscription         | Enables user to read subscription details using subscription Id  |
| Country-by-country-reporting        | POST        | /subscription/update-subscription       | Enables user to update subscription details                      |
| Country-by-country-reporting          | POST        | /validate-submission                    | Enables user to validate subscription details                    |
| Country-by-country-reporting          | POST        | /validation-result                      | Enables user to validate results                                 |
| Country-by-country-reporting              | GET         | /upscan/details/:uploadId               | Enables user to check file scanning details based on upload ID   |
| upscan              | GET         | /upscan/status/:uploadId                | Enables user to check file validation status based on upload id  |
| upscan              | POST        | /upscan/upload                          | Enables user to upload tax file for validation.                  |
| Country-by-country-reporting               | GET         | /files/:conversationId/details          | Enables user to check file details based on conversation ID      |
| Country-by-country-reporting               | GET         | /files/details                          | Enables user to check file details                               |
| Country-by-country-reporting               | GET         | /files/:conversationId/status           | Enables user to check tax file status                            |
| Country-by-country-reporting              | POST        | /submit                                 | Enables user to submit tax file                                  |
| Country-by-country-reporting  | POST        | /agent/subscription/create-subscription | Enables agents to create subscription                            |
| Country-by-country-reporting  | POST        | /agent/subscription/read-subscription   | Enables agents to read subscription                              |
| Country-by-country-reporting  | POST        | /agent/subscription/update-subscription | Enables agents to update subscription                            |
| Tax Enrolments                       | POST        | /tax-enrolments/service/:serviceName/enrolment                                  | Enrols a user synchronously for a given service name                                                      | 
| Address Lookup                       | GET         | /v2/uk/addresses                                                                | Returns a list of addresses that match a given postcode                                                   | 
| Email                                | POST        | /hmrc/email                                                                     | Sends an email to an email address                                                                        |
---


### Running the service

Service manager: CBCR_NEW_ALL

Port: 10024

Link: http://localhost:10024/send-a-country-by-country-report

---

### Prototype and tests

The prototype for this service can be viewed [here](https://cbc-reporting-prototype.herokuapp.com)

The journey tests can be found [here](https://github.com/hmrc/country-by-country-reporting-upload-ui-tests)

---


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").