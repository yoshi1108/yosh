" Vim global plugin for correcting typing mistakes
" Last Change:  2014 Feb 16
" Maintainer:   yosh <yoshi1108@gmail.com>
 
let s:save_cpo = &cpo
set cpo&vim

" ■ DEBUG中はコメントアウト
"if exists("g:loaded_webhome")
"	finish
"endif
"let g:loaded_webhome = 1
"map <unique> <Leader>h  <Plug>Webhome

command! Webhome :call s:Webhome()

if !executable('curl')
  echohl ErrorMsg | echomsg "require 'curl' command" | echohl None
  finish
endif

let s:home_url = "http://yoshi1108.web.fc2.com/"
let $http_proxy   = 'http://proxygate2.nic.nec.co.jp:8080'

function! s:Webhome()
   let s:V = vital#of('vital')
   let s:M = s:V.import('Web.Xml')

   " 結果
   let s:result = ""

   " URL先のHTMLをXMLにパース
   let s:xml = s:M.parseURL(s:home_url)
   
   "echo(s:xml.childNodes('body') )

   for node_body in s:xml.childNodes('body')
       "echo (node_body.childNode('div').value())
       "s:result = node_body.childNode('div').value()
       let s:cnt = 0
       "for node_div in node_body.childNodes('div')
       for node_div in node_body.childNodes('div')
           echo(node_div.childNode().value()) 
           "echo(node_div) 
    	   "s:cnt = s:cnt + 1
           "s:result = node_div.childNode().value()
           "s:result = s:result
       endfor
   endfor
   " ■ DEBUG中はコメントアウト
   ":vnew 'webhome'
   "call append('.', s:res.content)

   " ■ DEBUG用
   echo s:result
endfunction

let &cpo = s:save_cpo
unlet s:save_cpo

" ■ DEBUG用
:Webhome
