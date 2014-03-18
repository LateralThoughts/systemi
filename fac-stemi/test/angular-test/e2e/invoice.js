describe('Invoice page', function() {
  var ptor;

    beforeEach(function() {
        browser.get('http://127.0.0.1:9000/');
        ptor = protractor.getInstance();
    });

    it('test that submit button is present', function() {
        // Find the submit button
        expect(ptor.isElementPresent(by.css('button[type="submit"]'))).toBe(true);
    });
});