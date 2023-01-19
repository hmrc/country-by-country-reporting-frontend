# Country By Country Reporting Frontend

## Info

This service allows a user to upload the correct tax file which further goes for validation and once validated it finally gets uploaded.

This service has a corresponding back-end service, namely country-by-country-reporting.

### Dependencies

| Service           | Link                                                          |
|-------------------|---------------------------------------------------------------| 
| Address Lookup    | https://github.com/hmrc/address-lookup                        |
| Email             | https://github.com/hmrc/email                                 |
| Auth              | https://github.com/hmrc/auth                                  |
| Tax Enrolments    | https://github.com/hmrc/tax-enrolments                        |
| origin            | https://github.com/hmrc/country-by-country-reporting-frontend |

### Endpoints used

| Service             | HTTP Method | Route                                              | Purpose                                               |
|---------------------|-------------|----------------------------------------------------|-------------------------------------------------------|
| Tax Enrolments      | POST        | /tax-enrolments/service/:serviceName/enrolment     | Enrols a user synchronously for a given service name  |
| Email               | POST        | /hmrc/email                                        | Sends an email to an email address                    |
| subscription        | POST        | /subscription/read-subscription                    |                                                       |
| subscription        | POST        | /subscription/update-subscription                  |                                                       |
| validation          | POST        | /validate-submission                               |                                                       |
| validation          | POST        | /validation-result                                 |                                                       |
| upscan              | POST        | /callback                                          |                                                       |
| upscan              | GET         | /upscan/details/:uploadId                          |                                                       |
| upscan              | GET         | /upscan/status/:uploadId                           |                                                       |
| upscan              | POST        | /upscan/upload                                     |                                                       |
| files               | GET         | /files/:conversationId/details                     |                                                       |
| files               | GET         | /files/details                                     |                                                       |
| files               | GET         | /files/:conversationId/status                      |                                                       |
| submit              | POST        | /submit                                            |                                                       |
| Agent Subscription  | POST        | /agent/subscription/create-subscription            |                                                       |
| Agent Subscription  | POST        | /agent/subscription/read-subscription              |                                                       |
| Agent Subscription  | POST        | /agent/subscription/update-subscription            |                                                       |




## Running the service

Service Manager: CBCR_NEW_ALL

Port: 10024

Link: http://localhost:10024/send-a-country-by-country-report


## Tests and prototype

[View the prototype here](https://cross-border-arrangements.herokuapp.com)

| Repositories  | Link                                            |
|---------------|-------------------------------------------------|
| Journey tests |                                                 |
| Prototype     | https://cross-border-arrangements.herokuapp.com |







 
