" Vim global plugin for correcting typing mistakes
" Last Change:  2014 Feb 16
" Maintainer:   yosh <yoshi1108@gmail.com>
 
let s:save_cpo = &cpo
set cpo&vim

"if exists("g:loaded_webhome")
"	finish
"endif
let g:loaded_webhome = 1

if !executable('curl')
  echohl ErrorMsg | echomsg "require 'curl' command" | echohl None
  finish
endif

command! Webhome :call s:Webhome()

"map <unique> <Leader>h  <Plug>Webhome
"autocmd BufWriteCmd <buffer> call s:web_home()

let s:home_url = "http://yoshi1108.web.fc2.com/"

function! s:dump(node, syntax)
  let syntax = a:syntax
  if type(a:node) == 1
    if len(syntax) | exe "echohl ".syntax | endif
    echon webapi#html#decodeEntityReference(a:node)
    echohl None
  elseif type(a:node) == 3
    for n in a:node
      call s:dump(n, syntax)
    endfor
    return
  elseif type(a:node) == 4
      "echo a:node.name
      "echo a:node.attr
    let syndef = {'kt' : 'Type', 'mi' : 'Number', 'nb' : 'Statement', 'kp' : 'Statement', 'nn' : 'Define', 'nc' : 'Constant', 'no' : 'Constant', 'k'  : 'Include', 's'  : 'String', 's1' : 'String', 'err': 'Error', 'kd' : 'StorageClass', 'c1' : 'Comment', 'ss' : 'Delimiter', 'vi' : 'Identifier'}
    for a in keys(syndef)
      if has_key(a:node.attr, 'class') && a:node.attr['class'] == a | let syntax = syndef[a] | endif
    endfor
    if has_key(a:node.attr, 'class') && a:node.attr['class'] == 'line' | echon "\n" | endif
    for c in a:node.child
      call s:dump(c, syntax)
      unlet c
    endfor
  endif
endfunction

function! s:Webhome()
   :vnew 'webhome'
   let res = webapi#http#get(s:home_url)
   "let obj = webapi#json#decode(res.content)
   "let dom = webapi#html#parse(res)
   "for file in dom.childNodes('div')
   
   "for file in dom.childNodes()
   "  unlet! meta
   "  let meta = file.childNodes('div')
   "  if len(meta) > 1
   "    echo "URL:".meta[1].find('a').attr['href']
   "  endif
   "  echo "\n"
   "  call s:dump(file.find('pre'), '')
   "endfor
   "echo "hoge\n"
   
   call append('.', res.content)
endfunction

let &cpo = s:save_cpo
unlet s:save_cpo
