describe('Invoice page', function() {
  var ptor;

    beforeEach(function() {
        browser.get('http://127.0.0.1:9999/');
        ptor = protractor.getInstance();
    });

    it('test that submit button is present', function() {
        // Find the submit button
        expect(ptor.isElementPresent(by.css('button[type="submit"]'))).toBe(true);
    });

    it('test that initially only one task line is present', function() {
        ptor.findElements(by.repeater('taskline in tasklines')).then(function(elems) {
          expect(elems.length).toBe(1);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceDescription"]')).then(function(elems) {
          expect(elems.length).toBe(1);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceDays"]')).then(function(elems) {
          expect(elems.length).toBe(1);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceDailyRate"]')).then(function(elems) {
          expect(elems.length).toBe(1);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceTaxRate"]')).then(function(elems) {
          expect(elems.length).toBe(1);
        });
    });

    it('test that add task button is present', function() {
        expect(ptor.isElementPresent(by.css('button.add-task-btn'))).toBe(true);
    });

    it('test that clicking on add task button adds a task line and all inputs present', function() {
        element(by.css('button.add-task-btn')).click();
        ptor.findElements(protractor.By.repeater('taskline in tasklines')).then(function(elems) {
          expect(elems.length).toBe(2);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceDescription"]')).then(function(elems) {
          expect(elems.length).toBe(2);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceDays"]')).then(function(elems) {
          expect(elems.length).toBe(2);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceDailyRate"]')).then(function(elems) {
          expect(elems.length).toBe(2);
        });
        ptor.findElements(protractor.By.css('input[name="invoiceTaxRate"]')).then(function(elems) {
          expect(elems.length).toBe(2);
        });
    });

    it('test that clicking on add task button remove itself', function() {
        ptor.findElements(protractor.By.repeater('taskline in tasklines')).then(function(elems) {
            elems[0].findElement(protractor.By.css('button.add-task-btn')).then(function(el){
                expect(el.isDisplayed()).toBe(true);
            });
        });

        element(by.css('button.add-task-btn')).click();

        ptor.findElements(protractor.By.repeater('taskline in tasklines')).then(function(elems) {
            elems[0].findElement(protractor.By.css('button.add-task-btn')).then(function(el){
                expect(el.isDisplayed()).toBe(false);
            });
            elems[1].findElement(protractor.By.css('button.add-task-btn')).then(function(el){
                expect(el.isDisplayed()).toBe(true);
            });
        });
    });

    it('test that clicking on delete task button does not display for first line', function() {
        ptor.findElements(protractor.By.repeater('taskline in tasklines')).then(function(elems) {
            elems[0].findElement(protractor.By.css('button.delete-task-btn')).then(function(el){
                expect(el.isDisplayed()).toBe(false);
            });
        });

        element(by.css('button.add-task-btn')).click();

        ptor.findElements(protractor.By.repeater('taskline in tasklines')).then(function(elems) {
            elems[0].findElement(protractor.By.css('button.delete-task-btn')).then(function(el){
                expect(el.isDisplayed()).toBe(false);
            });
            elems[1].findElement(protractor.By.css('button.delete-task-btn')).then(function(el){
                expect(el.isDisplayed()).toBe(true);
            });
        });
    });
});