" Vim global plugin for correcting typing mistakes
" Last Change:  2014 Feb 16
" Maintainer:   yosh <yoshi1108@gmail.com>
 
let s:save_cpo = &cpo
set cpo&vim

if exists("g:loaded_webhome")
	finish
endif
let g:loaded_webhome = 1

if !executable('curl')
  echohl ErrorMsg | echomsg "require 'curl' command" | echohl None
  finish
endif

"map <unique> <Leader>h  <Plug>Webhome
"autocmd BufWriteCmd <buffer> call s:web_home()

let s:home_url = "http://yoshi1108.web.fc2.com/"

function! Webhome()
   let res = webapi#http#get(s:home_url)
   " new buffer 
   :new 
   " line add
   call append('.', res.content)
endfunction

let &cpo = s:save_cpo
unlet s:save_cpo
