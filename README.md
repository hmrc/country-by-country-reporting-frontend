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

| Service             | HTTP Method | Route                                   | Purpose                                                          |
|---------------------|-------------|-----------------------------------------|------------------------------------------------------------------|
| subscription        | POST        | /subscription/read-subscription         | Enables user to read subscription details using subscription Id  |
| subscription        | POST        | /subscription/update-subscription       | Enables user to update subscription details                      |
| validation          | POST        | /validate-submission                    | Enables user to validate subscription details                    |
| validation          | POST        | /validation-result                      | Enables user to validate results                                 |
| upscan              | GET         | /upscan/details/:uploadId               | Enables user to check file scanning details based on upload ID   |
| upscan              | GET         | /upscan/status/:uploadId                | Enables user to check file validation status based on upload id  |
| upscan              | POST        | /upscan/upload                          | Enables user to upload tax file for validation.                  |
| files               | GET         | /files/:conversationId/details          | Enables user to check file details based on conversation ID      |
| files               | GET         | /files/details                          | Enables user to check file details                               |
| files               | GET         | /files/:conversationId/status           | Enables user to check tax file status                            |
| submit              | POST        | /submit                                 | Enables user to submit tax file                                  |
| Agent Subscription  | POST        | /agent/subscription/create-subscription | Enables agents to create subscription                            |
| Agent Subscription  | POST        | /agent/subscription/read-subscription   | Enables agents to read subscription                              |
| Agent Subscription  | POST        | /agent/subscription/update-subscription | Enables agents to update subscription                            |




## Running the service

Service Manager: CBCR_NEW_ALL

Port: 10024

Link: http://localhost:10024/send-a-country-by-country-report


## Tests and prototype

[View the prototype here](https://cross-border-arrangements.herokuapp.com)

| Repositories  | Link                                                                 |
|---------------|----------------------------------------------------------------------|
| Journey tests | https://github.com/hmrc/country-by-country-reporting-upload-ui-tests |
| Prototype     | https://cross-border-arrangements.herokuapp.com                      |







 
