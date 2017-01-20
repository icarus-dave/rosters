import { browser, element, by, ExpectedConditions } from 'protractor';

describe('front App', function() {

  afterEach(function() {
      browser.executeScript('window.sessionStorage.clear();');
      browser.executeScript('window.localStorage.clear();');
  });

  it('redirect with unvalidated email',() => {
    browser.get("/")
    browser.waitForAngular()

    browser.ignoreSynchronization = true;
    
    browser.wait(() => browser.isElementPresent(by.css(".auth0-lock-submit")),10000)
    
    let emailEl = element(by.css(".auth0-lock-input-email input"))
    let passwordEl = element(by.css(".auth0-lock-input-password input"))

    emailEl.sendKeys("unverified-rosters@cdonald.nz")
    passwordEl.sendKeys("password")
    
    element(by.css('.auth0-lock-submit')).click()

    browser.wait(() => browser.getCurrentUrl().then(url => /login/.test(url)),10000)

    browser.ignoreSynchronization = false;

    let errorEl = element(by.css(".alert.alert-danger"))
    expect(errorEl.getText()).toContain("Please verify your email before logging in.")

    let loginAgain = element(by.css(".alert.alert-info"))
    expect(loginAgain.getText()).toContain("login again")
  });

  it('redirect with valid email',() => {
    browser.get("/")
    browser.waitForAngular()

    browser.ignoreSynchronization = true;
    
    browser.wait(() => browser.isElementPresent(by.css(".auth0-lock-submit")),10000)
    
    let emailEl = element(by.css(".auth0-lock-input-email input"))
    let passwordEl = element(by.css(".auth0-lock-input-password input"))

    emailEl.sendKeys("foo@cdonald.nz")
    passwordEl.sendKeys("password")
    
    element(by.css('.auth0-lock-submit')).click()

    browser.wait(() => browser.getCurrentUrl().then(url => /operators/.test(url)),10000)

    expect(element(by.css(".navbar-brand")).getText()).toEqual("Rosters")

  });

  //we've got an SSO session with auth0 and don't need to enter details again
  it('redirect with get parameter',() => {
    browser.get('/operators?foo=baz');
    browser.waitForAngular()

    browser.ignoreSynchronization = true;

    browser.wait(ExpectedConditions.textToBePresentInElement(element(by.css(".auth0-lock-social-button-text")),'foo@cdonald.nz'),10000)

    element(by.css(".auth0-lock-social-button")).click()

    browser.wait(() => browser.getCurrentUrl().then(url => /operators\?foo=baz/.test(url)),10000)

    expect(element(by.css(".navbar-brand")).getText()).toEqual("Rosters")

  });


});
