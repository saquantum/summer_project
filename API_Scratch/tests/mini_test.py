from Weather_Location_API_Data.common.Result import Result

def test_ok():
    r = Result.ok(123)
    assert r.is_success() and r.value == 123

def test_fail():
    r = Result.fail("err")
    assert r.is_failure() and r.error_msg == "err"
