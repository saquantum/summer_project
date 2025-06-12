from __future__ import annotations
from dataclasses import dataclass
from typing import Generic, Optional, TypeVar

T = TypeVar("T")

@dataclass(frozen=True)
class Result(Generic[T]):
    """
    泛型 Result: 成功時帶 value ; 失敗時帶 error 與 code

    Result 類別用於統一包裝函式返回結果。
    
    Attributes:
        success (bool): 表示操作是否成功。
        value: 操作成功時返回的數據，可能是任意型態。
        error_msg (str or Exception or dict): 操作失敗時的錯誤訊息或錯誤對象。
        code (int): （選擇性）錯誤碼，可用於標識特定類型的錯誤。
        traceback_str (str): （選擇性）錯誤的詳細追蹤信息（當需要時可以額外填入）。
    """

    # Declare the type of variables
    success: bool
    value: Optional[T] = None
    error_msg: Optional[str] = None
    code: Optional[int] = None
    traceback_str: Optional[str] = None

    # -------- Tool Methods -------- #
    def is_success(self) -> bool:
        """返回是否成功。"""
        return self.success

    def is_failure(self) -> bool:
        """返回是否失敗。"""
        return not self.success

    @staticmethod
    def ok(value) -> "Result[T]":
        """建立一個成功的 Result 對象。"""
        return Result(True, value=value, error_msg=None, traceback_str=None)

    @staticmethod
    def fail(error_msg, code: int | None = None, traceback_str: str | None = None) -> "Result[T]":
        """建立一個失敗的 Result 對象。"""
        return Result(False, error_msg=error_msg, code=code, traceback_str=traceback_str)

    def __repr__(self) -> str:
        return f"Success({self.value})" if self.success else f"Fail({self.error_msg})"

