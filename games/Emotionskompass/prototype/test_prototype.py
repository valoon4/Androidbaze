"""Browser-Smoke-Tests for the stand-alone M0.5 design prototype.
Run: python test_prototype.py
Requires Chromium + Playwright. No network and no backend needed.
"""
from pathlib import Path
from playwright.sync_api import sync_playwright

HTML=Path(__file__).with_name('index.html').read_text(encoding='utf-8')

def boot(browser,width=390,height=844):
    page=browser.new_page(viewport={'width':width,'height':height},device_scale_factor=1)
    errors=[]
    page.on('pageerror',lambda error:errors.append(str(error)))
    page.set_content(HTML,wait_until='load')
    assert page.locator('h1').count() == 1
    return page,errors

def run():
  with sync_playwright() as p:
    browser=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
    page,errors=boot(browser)
    page.screenshot(path=str(Path(__file__).with_name('home-mobile.png')),full_page=True)
    # Atlas search ranking: actual name first rather than mentions from explanations.
    page.locator('#mobileNav [data-main="atlas"]').click()
    page.locator('#atlasSearch').fill('frust')
    first=page.locator('#atlasResults .result-row').first
    assert 'Frustration' in first.inner_text(), 'Search should rank name matches first'
    first.click()
    assert page.locator('h1').inner_text()=='Frustration'
    page.locator('[data-action="compareFromWord"]').click()
    assert page.locator('h1').inner_text()=='Zwei Gefühle im Vergleich'
    assert 'Enttäuschung' in page.locator('.compare-pane').nth(1).inner_text()
    # Deep flow, mixed feelings, two emotions and free text
    page.locator('#mobileNav [data-main="home"]').click()
    page.locator('[data-action="flowStart"]').first.click()
    page.locator('[data-action="setMode"][data-value="deep"]').click()
    page.locator('[data-action="chooseEntry"][data-path="unknown"]').click()
    page.locator('[data-action="valence"][data-value="mixed"]').click()
    page.locator('[data-action="energy"][data-value="high"]').click()
    page.locator('[data-action="toChoose"]').click()
    page.locator('#choiceSearch').fill('frust')
    page.locator('[data-action="toggleEmotion"][data-id="frustration"]').click()
    assert page.locator('#int-frustration').count()==1
    page.locator('[data-action="choiceFamily"][data-id="trauer"]').click()
    page.locator('#choiceSearch').fill('enttäusch')
    page.locator('[data-action="toggleEmotion"][data-id="enttaeuschung"]').click()
    assert page.locator('#int-enttaeuschung').count()==1
    # Go to lexicon mid-check-in and return; retains two emotions.
    page.locator('[data-action="detail"][data-id="enttaeuschung"]').first.click()
    assert page.locator('h1').inner_text()=='Enttäuschung'
    page.locator('[data-action="back"]').click()
    assert page.locator('#int-frustration').count()==1
    assert page.locator('#int-enttaeuschung').count()==1
    page.locator('[data-action="afterChoose"]').click()
    assert page.locator('h1').inner_text()=='Was ist passiert?'
    page.locator('#reflectionInput').fill('Ein wichtiges Gespräch verlief anders als erhofft.')
    for i in range(6):
        page.locator('[data-action="nextReflection"]').click()
    assert page.locator('h1').inner_text()=='Dein persönlicher Blick'
    assert 'Frustration' in page.locator('.flow-card').inner_text()
    assert 'Enttäuschung' in page.locator('.flow-card').inner_text()
    page.locator('[data-action="saveDemo"]').click()
    assert page.locator('h1').inner_text()=='Dein Tagebuch'
    assert page.locator('.journal-entry').count()==2
    page.locator('.journal-entry').first.click()
    assert 'Frustration' in page.locator('main').inner_text()
    page.locator('[data-action="editEntry"]').click()
    page.locator('[data-action="editFeelings"]').click()
    page.locator('[data-action="removeEmotion"][data-id="frustration"]').click()
    page.locator('[data-action="afterChoose"]').click()
    # it goes through the reflection again; direct session save from the summary
    page.locator('[data-action="nextReflection"]').click()
    for _ in range(5):page.locator('[data-action="nextReflection"]').click()
    page.locator('[data-action="saveDemo"]').click()
    assert page.locator('.journal-entry').count()==2, 'Editing must not create a duplicate entry'
    page.locator('.journal-entry').first.click()
    assert 'Frustration' not in page.locator('.card').last.inner_text()
    page.locator('[data-action="deleteEntry"]').click()
    page.locator('[data-action="confirmDelete"]').click()
    assert page.locator('.journal-entry').count()==1
    assert not errors,errors
    page.close()
    # Short flow without any emotion must work.
    page,errors=boot(browser)
    page.locator('[data-action="flowStart"]').first.click()
    page.locator('[data-action="setMode"][data-value="quick"]').click()
    page.locator('[data-action="chooseEntry"][data-path="unknown"]').click()
    page.locator('[data-action="flowSkipSense"]').click()
    page.locator('[data-action="flowSkipChoose"]').click()
    assert 'Noch kein Gefühl benannt' in page.locator('main').inner_text()
    page.locator('[data-action="discardDraft"]').click()
    assert page.locator('h1').count()==1
    assert not errors,errors
    page.close()
    # Situation-first flow preserves initial text and skips duplicative question
    page,errors=boot(browser)
    page.locator('[data-action="flowStart"]').first.click()
    page.locator('[data-action="chooseEntry"][data-path="situation"]').click()
    assert page.locator('h1').inner_text()=='Was ist passiert?'
    page.locator('#introSituation').fill('Im Team gab es ein Missverständnis.')
    page.locator('[data-action="fromIntro"]').click()
    page.locator('[data-action="flowSkipSense"]').click()
    page.locator('[data-action="flowSkipChoose"]').click()
    assert page.locator('h1').inner_text()=='Was ging dir durch den Kopf?'
    for _ in range(5): page.locator('[data-action="nextReflection"]').click()
    assert 'Im Team gab es ein Missverständnis.' in page.locator('main').inner_text()
    assert not errors,errors
    page.close()
    # Desktop breakpoint and screenshot
    page,errors=boot(browser,1280,900)
    assert page.locator('.rail').is_visible()
    page.screenshot(path=str(Path(__file__).with_name('home-desktop.png')),full_page=True)
    page.locator('#desktopNav [data-main="atlas"]').click()
    page.screenshot(path=str(Path(__file__).with_name('atlas-desktop.png')),full_page=True)
    assert not errors,errors
    page.close()
    browser.close()
    print('PASS – atlas search, comparison, deep/quick flows, multiple emotions, context return, reflection, save/edit/delete, situation-first, responsive screens')

if __name__=='__main__':run()