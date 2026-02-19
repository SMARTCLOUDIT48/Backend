function deleteUser(userId) {
  if (!confirm("⚠ 회원을 DB에서 완전히 삭제합니다.\n되돌릴 수 없습니다.")) return;

  const btn = event.target;
  btn.classList.add("disabled");
  btn.innerText = "처리 중...";

  fetch(`/admin/users/${userId}/delete`, {
    method: "POST"
  }).then(() => {
    alert("탈퇴 처리되었습니다.");
    location.href = "/admin/users";
  });
}
